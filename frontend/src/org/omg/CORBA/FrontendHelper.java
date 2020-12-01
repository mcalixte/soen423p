package org.omg.CORBA;


/**
* org/omg/CORBA/FrontendHelper.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from CORBA.idl
* Saturday, November 28, 2020 8:36:11 o'clock PM EST
*/

abstract public class FrontendHelper
{
  private static String  _id = "IDL:CORBA/Frontend:1.0";

  public static void insert (org.omg.CORBA.Any a, org.omg.CORBA.Frontend that)
  {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
  }

  public static org.omg.CORBA.Frontend extract (org.omg.CORBA.Any a)
  {
    return read (a.create_input_stream ());
  }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  synchronized public static org.omg.CORBA.TypeCode type ()
  {
    if (__typeCode == null)
    {
      __typeCode = org.omg.CORBA.ORB.init ().create_interface_tc (org.omg.CORBA.FrontendHelper.id (), "Frontend");
    }
    return __typeCode;
  }

  public static String id ()
  {
    return _id;
  }

  public static org.omg.CORBA.Frontend read (org.omg.CORBA.portable.InputStream istream)
  {
    return narrow (istream.read_Object (_FrontendStub.class));
  }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, org.omg.CORBA.Frontend value)
  {
    ostream.write_Object ((org.omg.CORBA.Object) value);
  }

  public static org.omg.CORBA.Frontend narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof org.omg.CORBA.Frontend)
      return (org.omg.CORBA.Frontend)obj;
    else if (!obj._is_a (id ()))
      throw new org.omg.CORBA.BAD_PARAM ();
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      org.omg.CORBA._FrontendStub stub = new org.omg.CORBA._FrontendStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

  public static org.omg.CORBA.Frontend unchecked_narrow (org.omg.CORBA.Object obj)
  {
    if (obj == null)
      return null;
    else if (obj instanceof org.omg.CORBA.Frontend)
      return (org.omg.CORBA.Frontend)obj;
    else
    {
      org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl)obj)._get_delegate ();
      org.omg.CORBA._FrontendStub stub = new org.omg.CORBA._FrontendStub ();
      stub._set_delegate(delegate);
      return stub;
    }
  }

}
