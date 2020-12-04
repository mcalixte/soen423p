import networkEntities.RegisteredReplica;
import replica.ReplicaResponse;

import java.util.*;
import java.util.concurrent.Semaphore;

public class ReplicaErrorTracker {

    final private HashMap<RegisteredReplica, String> answers;
    final private int sequenceNumber;

    private List<RegisteredReplica> crashedReplicas = new ArrayList<>();
    private List<RegisteredReplica> erroneousReplicas  = new ArrayList<>();
    private List<ReplicaResponse> validReplicas  = new ArrayList<>();

    private String storedAnswers;

    public ReplicaErrorTracker(int sequenceID) {
        this.answers = new HashMap<>();
        sequenceNumber = sequenceID;
    }

    public void trackPotentialErrors(List<ReplicaResponse> replicaResponses) {
        if(replicaResponses.size() < 3)
            findMissingReplica(replicaResponses);
        else
            findPossibleErroneousReplica(replicaResponses);
    }

    public void findMissingReplica(List<ReplicaResponse> replicaResponses){
        boolean replica1 = false;
        boolean replica2 = false;
        boolean replica3 = false;

        for(ReplicaResponse response : replicaResponses){
            if(response.getReplicaID()==RegisteredReplica.ReplicaS1)
                replica1 = true;
            if(response.getReplicaID()==RegisteredReplica.ReplicaS2)
                replica2 = true;
            if(response.getReplicaID()==RegisteredReplica.ReplicaS3)
                replica3 = true;
        }

        if(replica1 == false){
            crashedReplicas.add(RegisteredReplica.ReplicaS1);
        }
        else if(replica2 == false){
            crashedReplicas.add(RegisteredReplica.ReplicaS2);
        }
        else if(replica3 == false){
            crashedReplicas.add(RegisteredReplica.ReplicaS3);
        }
    }

    public void findPossibleErroneousReplica(List<ReplicaResponse> replicaResponses) {
        if (replicaResponses.size() == 3) {
            List<String> responses = new ArrayList<>();
            for (ReplicaResponse response : replicaResponses) {
                for (Map.Entry<String, String> entry : response.getResponse().entrySet()) {
                    responses.add(entry.getValue());
                }
            }

            if (!responses.get(0).equalsIgnoreCase(responses.get(1))) {
                if (!responses.get(0).equalsIgnoreCase(responses.get(2))) {
                    erroneousReplicas.add(replicaResponses.get(0).getReplicaID());
                    validReplicas.add(replicaResponses.get(1));
                    validReplicas.add(replicaResponses.get(2));
                } else if (responses.get(0).equalsIgnoreCase(responses.get(2))) {
                    validReplicas.add(replicaResponses.get(0));
                    erroneousReplicas.add(replicaResponses.get(1).getReplicaID());
                    validReplicas.add(replicaResponses.get(2));
                }
            }
            if (responses.get(0).equalsIgnoreCase(responses.get(1))) {
                if (!responses.get(0).equalsIgnoreCase(responses.get(2))) {
                    validReplicas.add(replicaResponses.get(0));
                    validReplicas.add(replicaResponses.get(1));
                    erroneousReplicas.add(replicaResponses.get(2).getReplicaID());
                }
            }
        }
    }

    public List<RegisteredReplica> getCrashedReplicas(){
        return crashedReplicas;
    }

    public List<RegisteredReplica> getErroneousReplicas(){
        return erroneousReplicas;
    }

    public List<ReplicaResponse> getValidReplicas() {return validReplicas;}
}