package pt.ulisboa.tecnico.classes.classserver;

import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateRequest;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerClassServer.PropagateStateResponse;
import pt.ulisboa.tecnico.classes.contract.classserver.ClassServerServiceGrpc;
import pt.ulisboa.tecnico.classes.contract.ClassesDefinitions.ResponseCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class ClassServerServiceImpl extends ClassServerServiceGrpc.ClassServerServiceImplBase {

    private final Class classes = Class.getInstance();

    private final boolean debugFlag;

    private final String qualifier;

    private static final Logger LOGGER = Logger.getLogger(Class.class.getName());

    //Constructors
    public ClassServerServiceImpl(boolean debugFlag, String qualifier) {
        this.debugFlag = debugFlag;
        this.qualifier = qualifier;
    }

    //Methods
    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
        if (this.debugFlag) {
            LOGGER.info("Propagating State...");
        }

        //Create Response
        PropagateStateResponse.Builder response = PropagateStateResponse.newBuilder();
        synchronized (classes) {
            if (!classes.getActive()) {
                response.setCode(ResponseCode.DISABLED_SERVER);
            }
            else {
                ConcurrentHashMap<String, String[]> studentsEnrolled = new ConcurrentHashMap<>(this.classes.getStudentsEnrolled());
                ConcurrentHashMap<String, String[]> studentsUnrolled = new ConcurrentHashMap<>(this.classes.getStudentsUnrolled());
                for (ClassesDefinitions.Student student : request.getClassState().getEnrolledList()) {
                    String studentId = student.getStudentId();
                    if (!this.classes.isEnrolled(studentId) || (this.classes.isUnrolled(studentId) && this.classes.getTimeStamp(studentId).compareTo(student.getTimeStamp()) < 0)) {
                        String[] value = {student.getStudentName(), student.getTimeStamp()};
                        studentsEnrolled.put(studentId, value);
                        studentsUnrolled.remove(studentId);
                    }
                }
                for (ClassesDefinitions.Student student : request.getClassState().getDiscardedList()) {
                    String studentId = student.getStudentId();
                    if (!this.classes.isUnrolled(studentId) || (this.classes.isEnrolled(studentId) && this.classes.getTimeStamp(studentId).compareTo(student.getTimeStamp()) < 0)) {
                        String[] value = {student.getStudentName(), student.getTimeStamp()};
                        studentsUnrolled.put(studentId, value);
                        studentsEnrolled.remove(studentId);
                    }
                }

                int numEnrolled = studentsEnrolled.size();
                List<Map.Entry<String, String[]>> sortedEnrolledList = new LinkedList<>(studentsEnrolled.entrySet());
                Collections.sort(sortedEnrolledList, new Comparator<Map.Entry<String, String[]>>() {
                    public int compare(Map.Entry<String, String[]> o1, Map.Entry<String, String[]> o2) {
                        return (o1.getValue()[1]).compareTo(o2.getValue()[1]);
                    }
                });

                int remainingCapacity = this.classes.getCapacity();
                for (Map.Entry<String, String[]> entry : sortedEnrolledList) {
                    if (remainingCapacity > 0 && (request.getClassState().getOpenEnrollments() || request.getClassState().getClosedEnrollments().compareTo(entry.getValue()[1]) >= 0)) {
                        remainingCapacity--;
                    }
                    else {
                        String studentId = entry.getKey();
                        String studentName = entry.getValue()[0];
                        String[] value = {studentName, LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))};
                        studentsEnrolled.remove(entry.getKey());
                        studentsUnrolled.put(studentId, value);
                    }
                }
                this.classes.setStudentsEnrolled(studentsEnrolled);
                this.classes.setStudentsUnrolled(studentsUnrolled);

                if (this.qualifier.equals("S")) {
                    this.classes.setCapacity(request.getClassState().getCapacity());
                    this.classes.setEnrollmentsOpen(request.getClassState().getOpenEnrollments());
                    this.classes.setClosedEnrollments(request.getClassState().getClosedEnrollments());
                    this.classes.setCapacity(request.getClassState().getCapacity());
                }

                response.setCode(ResponseCode.OK).build();
            }
        }

        //Send Response
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();

        if (this.debugFlag) {
            LOGGER.info("Propagate State finished.");
        }
    }
}