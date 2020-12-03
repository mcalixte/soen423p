package org.omg.CORBA;


/**
* org/omg/CORBA/_IFrontendStub.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CORBA.idl
* Wednesday, December 2, 2020 1:27:25 o'clock AM EST
*/

public class _IFrontendStub extends org.omg.CORBA.portable.ObjectImpl implements org.omg.CORBA.IFrontend
{

  public void softwareFailure (String userID)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("softwareFailure", true);
                $out.write_string (userID);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                softwareFailure (userID        );
            } finally {
                _releaseReply ($in);
            }
  } // softwareFailure

  public void replicaCrash (String userID)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("replicaCrash", true);
                $out.write_string (userID);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                replicaCrash (userID        );
            } finally {
                _releaseReply ($in);
            }
  } // replicaCrash

  public String createAddItem (String userID, String itemID, String itemName, int quantity, double price)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("createAddItem", true);
                $out.write_string (userID);
                $out.write_string (itemID);
                $out.write_string (itemName);
                $out.write_long (quantity);
                $out.write_double (price);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return createAddItem (userID, itemID, itemName, quantity, price        );
            } finally {
                _releaseReply ($in);
            }
  } // createAddItem

  public String createRemoveItem (String userID, String itemID, int quantity)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("createRemoveItem", true);
                $out.write_string (userID);
                $out.write_string (itemID);
                $out.write_long (quantity);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return createRemoveItem (userID, itemID, quantity        );
            } finally {
                _releaseReply ($in);
            }
  } // createRemoveItem

  public String createListItems (String userID)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("createListItems", true);
                $out.write_string (userID);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return createListItems (userID        );
            } finally {
                _releaseReply ($in);
            }
  } // createListItems

  public String createPurchaseItems (String userID, String itemID, String dateOfReturn)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("createPurchaseItems", true);
                $out.write_string (userID);
                $out.write_string (itemID);
                $out.write_string (dateOfReturn);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return createPurchaseItems (userID, itemID, dateOfReturn        );
            } finally {
                _releaseReply ($in);
            }
  } // createPurchaseItems

  public String createReturnItems (String userID, String itemID, String dateOfReturn)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("createReturnItems", true);
                $out.write_string (userID);
                $out.write_string (itemID);
                $out.write_string (dateOfReturn);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return createReturnItems (userID, itemID, dateOfReturn        );
            } finally {
                _releaseReply ($in);
            }
  } // createReturnItems

  public String createExchangeItem (String userID, String newItemID, String oldItemID, String dateOfExchange)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("createExchangeItem", true);
                $out.write_string (userID);
                $out.write_string (newItemID);
                $out.write_string (oldItemID);
                $out.write_string (dateOfExchange);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return createExchangeItem (userID, newItemID, oldItemID, dateOfExchange        );
            } finally {
                _releaseReply ($in);
            }
  } // createExchangeItem

  public String createFindItem (String userID, String itemName)
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("createFindItem", true);
                $out.write_string (userID);
                $out.write_string (itemName);
                $in = _invoke ($out);
                String $result = $in.read_string ();
                return $result;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                return createFindItem (userID, itemName        );
            } finally {
                _releaseReply ($in);
            }
  } // createFindItem

  public void shutdown ()
  {
            org.omg.CORBA.portable.InputStream $in = null;
            try {
                org.omg.CORBA.portable.OutputStream $out = _request ("shutdown", false);
                $in = _invoke ($out);
                return;
            } catch (org.omg.CORBA.portable.ApplicationException $ex) {
                $in = $ex.getInputStream ();
                String _id = $ex.getId ();
                throw new org.omg.CORBA.MARSHAL (_id);
            } catch (org.omg.CORBA.portable.RemarshalException $rm) {
                shutdown (        );
            } finally {
                _releaseReply ($in);
            }
  } // shutdown

  // Type-specific CORBA::Object operations
  private static String[] __ids = {
    "IDL:CORBA/IFrontend:1.0"};

  public String[] _ids ()
  {
    return (String[])__ids.clone ();
  }

  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException
  {
     String str = s.readUTF ();
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     org.omg.CORBA.Object obj = orb.string_to_object (str);
     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();
     _set_delegate (delegate);
   } finally {
     orb.destroy() ;
   }
  }

  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException
  {
     String[] args = null;
     java.util.Properties props = null;
     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);
   try {
     String str = orb.object_to_string (this);
     s.writeUTF (str);
   } finally {
     orb.destroy() ;
   }
  }
} // class _IFrontendStub
