package com.apollo.gps_server.WebService;



import org.kobjects.base64.Base64;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.MarshalBase64;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Administrator on 14-6-9.
 */

public final class Webservice {
    private static HttpTransportSE httpTransportSE;
    private HttpTransportSE httpTransportSE2;
    public static void InitService(String Url) {

        if (httpTransportSE == null) {
            httpTransportSE = new HttpTransportSE(Url,15000);

        } else {
            httpTransportSE.setUrl(Url);
            httpTransportSE.reset();
        }

    }

    public Webservice(String url,int timeout)
    {
        httpTransportSE2=  new HttpTransportSE(url,timeout);
    }

    public String PDA_GetInterFaceForStringNew(PropertyInfo[] propertyInfos, String methname) {

        SoapSerializationEnvelope soapSerializationEnvelope;
        soapSerializationEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        SoapObject soapObject = new SoapObject("http://tempuri.org/", methname);
        if (propertyInfos != null) {
            for (int i = 0; i < propertyInfos.length; i++) {
                soapObject.addProperty(propertyInfos[i]);
            }
        }
        MarshalBase64 md = new MarshalBase64();
        md.register(soapSerializationEnvelope);
        soapSerializationEnvelope.bodyOut = soapObject;
        soapSerializationEnvelope.dotNet = true;
        soapSerializationEnvelope.setOutputSoapObject(soapObject);
        try {

            httpTransportSE2.call("http://tempuri.org/IPDA/" + methname, soapSerializationEnvelope);
            if (soapSerializationEnvelope.getResponse() != null) {
                // 获取服务器响应返回的SOAP消息
                SoapObject result = (SoapObject) soapSerializationEnvelope.bodyIn;


                return result.getProperty(0).toString();
            }
        } catch (IOException e) {
            return "-1";
        } catch (XmlPullParserException e) {
            return "-1";
        }catch (Exception e)
        {
            return "-1";
        }

        return "-1";
    }


    public static String PDA_GetInterFaceForString(PropertyInfo[] propertyInfos, String methname) {

        SoapSerializationEnvelope soapSerializationEnvelope;
        soapSerializationEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        SoapObject soapObject = new SoapObject("http://tempuri.org/", methname);
        if (propertyInfos != null) {
            for (int i = 0; i < propertyInfos.length; i++) {
                soapObject.addProperty(propertyInfos[i]);
            }
        }
        MarshalBase64 md = new MarshalBase64();
        md.register(soapSerializationEnvelope);
        soapSerializationEnvelope.bodyOut = soapObject;
        soapSerializationEnvelope.dotNet = true;
        soapSerializationEnvelope.setOutputSoapObject(soapObject);
        try {

            httpTransportSE.call("http://tempuri.org/" + methname, soapSerializationEnvelope);
            if (soapSerializationEnvelope.getResponse() != null) {
                // 获取服务器响应返回的SOAP消息
                SoapObject result = (SoapObject) soapSerializationEnvelope.bodyIn;


                return result.getProperty(0).toString();
            }
        } catch (IOException e) {
            return "-1";
        } catch (XmlPullParserException e) {
            return "-1";
        }catch (Exception e)
        {
            return "-1";
        }

        return "-1";
    }





    public static byte[] PDA_GetInterFaceForbyte(PropertyInfo[] propertyInfos, String methname) {

        SoapSerializationEnvelope soapSerializationEnvelope;
        soapSerializationEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        SoapObject soapObject = new SoapObject("http://tempuri.org/", methname);
        if (propertyInfos != null) {
            for (int i = 0; i < propertyInfos.length; i++) {
                soapObject.addProperty(propertyInfos[i]);
            }
        }
        MarshalBase64 md = new MarshalBase64();
        md.register(soapSerializationEnvelope);
        soapSerializationEnvelope.bodyOut = soapObject;
        soapSerializationEnvelope.dotNet = true;
        soapSerializationEnvelope.setOutputSoapObject(soapObject);
        try {

            httpTransportSE.call("http://tempuri.org/IPDA/" + methname, soapSerializationEnvelope);
            if (soapSerializationEnvelope.getResponse() != null) {
                // 获取服务器响应返回的SOAP消息
                SoapObject result = (SoapObject) soapSerializationEnvelope.bodyIn;
                byte[] byte_result = Base64.decode(result.getProperty(0).toString());

                return byte_result;
            }
        } catch (IOException e) {
            return null;
        } catch (XmlPullParserException e) {
            return null;
        }catch (Exception e)
        {
            return null;
        }

        return null;
    }


    public byte[] PDA_GetInterFaceForbyteNew(PropertyInfo[] propertyInfos, String methname) {

        SoapSerializationEnvelope soapSerializationEnvelope;
        soapSerializationEnvelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        SoapObject soapObject = new SoapObject("http://tempuri.org/", methname);
        if (propertyInfos != null) {
            for (int i = 0; i < propertyInfos.length; i++) {
                soapObject.addProperty(propertyInfos[i]);
            }
        }
        MarshalBase64 md = new MarshalBase64();
        md.register(soapSerializationEnvelope);
        soapSerializationEnvelope.bodyOut = soapObject;
        soapSerializationEnvelope.dotNet = true;
        soapSerializationEnvelope.setOutputSoapObject(soapObject);
        try {

            httpTransportSE2.call("http://tempuri.org/IPDA/" + methname, soapSerializationEnvelope);
            if (soapSerializationEnvelope.getResponse() != null) {
                // 获取服务器响应返回的SOAP消息
                SoapObject result = (SoapObject) soapSerializationEnvelope.bodyIn;
                byte[] byte_result = Base64.decode(result.getProperty(0).toString());

                return byte_result;
            }
        } catch (IOException e) {
            return null;
        } catch (XmlPullParserException e) {
            return null;
        }catch (Exception e)
        {
            return null;
        }

        return null;
    }


}
