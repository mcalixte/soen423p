import networkEntities.RegisteredReplica;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

public class ConsensusTracker {

    final private HashMap<RegisteredReplica, String> answers;
    final private int sequenceNumber;
    final private Semaphore complete;

    final private LinkedList<RegisteredReplica> inError = new LinkedList<>();
    private String storedAnswers;

    public ConsensusTracker(int consensusCountNeeded, int sequenceID) {
        this.answers = new HashMap<>();
        complete = new Semaphore(0-consensusCountNeeded);
        sequenceNumber = sequenceID;
    }

    public void addRequestConsensus(RegisteredReplica replica, int sequenceID, String answer) {
        if (answers.isEmpty()) {
            storedAnswers = answer;
        }

        if (sequenceNumber == sequenceID && !answers.containsKey(replica)) {
            answers.put(replica, answer);
            complete.release();
        } else {
            inError.add(replica);
        }

        int index = 0;
        int counter[] = new int[answers.size()];
        for (String potential : answers.values()) {
            for (String suspect : answers.values()) {
                if (potential.equals(suspect)) {
                    counter[index] += 1;
                }
            }
            index++;
        }

        int max = 0;
        for (int i = 0; i < answers.size(); i++) {
            if (max < counter[i]) {
                max = counter[i];
            }
        }

        int ticker = 0;
        for (String potential : answers.values()) {
            if (max == counter[ticker]) {
                if (!storedAnswers.equals(potential)) {

                    for (RegisteredReplica suspect : RegisteredReplica.values()) {

                        if (answers.containsKey(suspect) && answers.get(suspect).equals(storedAnswers)) {
                            inError.add(suspect);
                        }
                    }
                    storedAnswers = potential;
                }

                break;
            }

            ticker++;
        }

        if (answers.size() > 2
                && !answer.equals(storedAnswers)) {
            inError.add(replica);
        }
    }

    public void Wait() throws InterruptedException {
        complete.acquire();
    }

    public boolean contains(RegisteredReplica instance) {
        return answers.containsKey(instance);
    }

    public String getAnswer() {
        return storedAnswers;
    }

    public LinkedList<RegisteredReplica> getFailures() {
        return inError;
    }

    public LinkedList<RegisteredReplica> getAnswers() {
        LinkedList<RegisteredReplica> respondents = new LinkedList<>();

        for (RegisteredReplica suspect : RegisteredReplica.values()) {
            if (suspect == RegisteredReplica.EVERYONE) {
                continue;
            }

            if (answers.containsKey(suspect)) {
                respondents.add(suspect);
            }
        }

        return respondents;
    }

    public LinkedList<RegisteredReplica> getMissingAnswers() {
        LinkedList<RegisteredReplica> missing = new LinkedList<>();

        for (RegisteredReplica suspect : RegisteredReplica.values()) {
            if (suspect == RegisteredReplica.EVERYONE) {
                continue;
            }

            if (!answers.containsKey(suspect)) {
                missing.add(suspect);
            }
        }

        return missing;
    }
}