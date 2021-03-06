package org.omg.CORBA;


/**
* org/omg/CORBA/IFrontendPOA.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CORBA.idl
* Wednesday, December 2, 2020 1:27:25 o'clock AM EST
*/

public abstract class IFrontendPOA extends org.omg.PortableServer.Servant
 implements org.omg.CORBA.IFrontendOperations, org.omg.CORBA.portable.InvokeHandler
{

  // Constructors

  private static java.util.Hashtable _methods = new java.util.Hashtable ();
  static
  {
    _methods.put ("softwareFailure", new java.lang.Integer (0));
    _methods.put ("replicaCrash", new java.lang.Integer (1));
    _methods.put ("createAddItem", new java.lang.Integer (2));
    _methods.put ("createRemoveItem", new java.lang.Integer (3));
    _methods.put ("createListItems", new java.lang.Integer (4));
    _methods.put ("createPurchaseItems", new java.lang.Integer (5));
    _methods.put ("createReturnItems", new java.lang.Integer (6));
    _methods.put ("createExchangeItem", new java.lang.Integer (7));
    _methods.put ("createFindItem", new java.lang.Integer (8));
    _methods.put ("shutdown", new java.lang.Integer (9));
  }

  public org.omg.CORBA.portable.OutputStream _invoke (String $method,
                                org.omg.CORBA.portable.InputStream in,
                                org.omg.CORBA.portable.ResponseHandler $rh)
  {
    org.omg.CORBA.portable.OutputStream out = null;
    java.lang.Integer __method = (java.lang.Integer)_methods.get ($method);
    if (__method == null)
      throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);

    switch (__method.intValue ())
    {
       case 0:  // org/omg/CORBA/IFrontend/softwareFailure
       {
         String userID = in.read_string ();
         this.softwareFailure (userID);
         out = $rh.createReply();
         break;
       }

       case 1:  // org/omg/CORBA/IFrontend/replicaCrash
       {
         String userID = in.read_string ();
         this.replicaCrash (userID);
         out = $rh.createReply();
         break;
       }

       case 2:  // org/omg/CORBA/IFrontend/createAddItem
       {
         String userID = in.read_string ();
         String itemID = in.read_string ();
         String itemName = in.read_string ();
         int quantity = in.read_long ();
         double price = in.read_double ();
         String $result = null;
         $result = this.createAddItem (userID, itemID, itemName, quantity, price);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 3:  // org/omg/CORBA/IFrontend/createRemoveItem
       {
         String userID = in.read_string ();
         String itemID = in.read_string ();
         int quantity = in.read_long ();
         String $result = null;
         $result = this.createRemoveItem (userID, itemID, quantity);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 4:  // org/omg/CORBA/IFrontend/createListItems
       {
         String userID = in.read_string ();
         String $result = null;
         $result = this.createListItems (userID);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 5:  // org/omg/CORBA/IFrontend/createPurchaseItems
       {
         String userID = in.read_string ();
         String itemID = in.read_string ();
         String dateOfReturn = in.read_string ();
         String $result = null;
         $result = this.createPurchaseItems (userID, itemID, dateOfReturn);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 6:  // org/omg/CORBA/IFrontend/createReturnItems
       {
         String userID = in.read_string ();
         String itemID = in.read_string ();
         String dateOfReturn = in.read_string ();
         String $result = null;
         $result = this.createReturnItems (userID, itemID, dateOfReturn);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 7:  // org/omg/CORBA/IFrontend/createExchangeItem
       {
         String userID = in.read_string ();
         String newItemID = in.read_string ();
         String oldItemID = in.read_string ();
         String dateOfExchange = in.read_string ();
         String $result = null;
         $result = this.createExchangeItem (userID, newItemID, oldItemID, dateOfExchange);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 8:  // org/omg/CORBA/IFrontend/createFindItem
       {
         String userID = in.read_string ();
         String itemName = in.read_string ();
         String $result = null;
         $result = this.createFindItem (userID, itemName);
         out = $rh.createReply();
         out.write_string ($result);
         break;
       }

       case 9:  // org/omg/CORBA/IFrontend/shutdown
       {
         this.shutdown ();
         out = $rh.createReply();
         break;
       }

       default:
         throw new org.omg.CORBA.BAD_OPERATION (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
    }

    return out;
  } // _invoke

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:CORBA/IFrontend:1.0"};

  public String[] _all_interfaces (org.omg.PortableServer.POA poa, byte[] objectId)
  {
    return (String[])__ids.clone ();
  }

  public IFrontend _this() 
  {
    return IFrontendHelper.narrow(
    super._this_object());
  }

  public IFrontend _this(org.omg.CORBA.ORB orb) 
  {
    return IFrontendHelper.narrow(
    super._this_object(orb));
  }


} // class IFrontendPOA
