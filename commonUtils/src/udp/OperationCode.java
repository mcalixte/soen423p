package udp;

public enum OperationCode {
        INVALID(-1),

        // For FE-SEQUENCER-REPLICA Communication
        ADD_ITEM(1001),
        REMOVE_ITEM(1002),
        LIST_ITEMS(1003),

        PURCHASE_ITEM(1004),
        FIND_ITEM(1005),
        RETURN_ITEM(1006),
        EXCHANGE_ITEM(1007),

        // For FE-RM
        NO_RESPONSE_RECEIVED_NOTIFICATION(3000),
        ACK_NO_RESPONSE_RECEIVED_NOTIFICATION(3002),
        FAULTY_RESP_RECEIVED_NOTIFICATION(3003),
        ACK_FAULTY_RESP_RECEIVED_NOTIFICATION(3004),

        //For RM-RE
        RESTORE_DATA_WITH_ORDERED_REQUESTS_NOTIFICATION(3005),
        ACK_RESTORE_DATA_WITH_ORDERED_REQUESTS_NOTIFICATION(3006),
        RESTART_ORDER_NOTIFICATION(3007),
        ACK_RESTART_ORDER_NOTIFICATION(3008);

        final private int codeValue;

        private OperationCode(int val) {
            codeValue = val;
        }



        public OperationCode toAck(){
            if( codeValue >= 3000 ){
                switch( this ){
                    case NO_RESPONSE_RECEIVED_NOTIFICATION: return ACK_NO_RESPONSE_RECEIVED_NOTIFICATION;
                    case FAULTY_RESP_RECEIVED_NOTIFICATION: return ACK_FAULTY_RESP_RECEIVED_NOTIFICATION;
                    case RESTORE_DATA_WITH_ORDERED_REQUESTS_NOTIFICATION: return ACK_RESTORE_DATA_WITH_ORDERED_REQUESTS_NOTIFICATION;
                    case RESTART_ORDER_NOTIFICATION: return ACK_RESTART_ORDER_NOTIFICATION;

                    default: return INVALID;
                }
            }
            return this;
        }

         public String toString() {
           return "" + codeValue;
         }

        static public OperationCode fromString(String val) {
            int newVal = Integer.parseInt(val);
            for (OperationCode opcode : OperationCode.values()) {
                if (newVal == opcode.codeValue) {
                    return opcode;
                }
            }

            return INVALID;
        }
}
