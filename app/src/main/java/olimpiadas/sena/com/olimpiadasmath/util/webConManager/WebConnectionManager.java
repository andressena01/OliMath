package olimpiadas.sena.com.olimpiadasmath.util.webConManager;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.mime.MultipartEntityBuilder;
import cz.msebera.android.httpclient.message.BasicNameValuePair;


/**
 * Created by defytek on 3/1/17.
 */

public class WebConnectionManager implements WebConnection.WebConnectionListener{

    private String TAG = "WebConnectionManager";
    private final String url = "URL DE CONSULTA";




    public interface WebConnectionManagerListener {
        void webRequestComplete(Response response);

    }

    private static WebConnectionManager webConnectionManager = null;
    private String session = "";
    private WebConnection webConnection = null;
    private WebConnectionManagerListener webConnectionManagerListener;




    public enum OperationType {
        START_SESSION,

        LOGIN;

        public String getName() {
            switch (this) {
                case START_SESSION:
                    return "start-session/";

                case LOGIN:
                    return "login/";

                default:
                    return null;
            }
        }

        public static OperationType fromString(String type){
            switch (type) {
                case "start-session/":
                    return START_SESSION;

                case "login/":
                    return LOGIN;

                default:
                    return null;
            }

        }
    }

    @Override
    public void webConnectionComplete(String type, String resp) {
        Log.d(TAG,"webCon Response = " + resp);

        Response response = new Response(type,resp);
        Log.d(TAG,"Response = " +  response.toString());
        if(webConnectionManagerListener!=null){
            webConnectionManagerListener.webRequestComplete(response);
        }

    }


    public void setWebConnectionManagerListener(WebConnectionManagerListener webConnectionManagerListener) {
        this.webConnectionManagerListener = webConnectionManagerListener;
    }

    private WebConnectionManager(){

        webConnection = new WebConnection(this);

    }

    public static WebConnectionManager getWebConnectionManager(){
        if(webConnectionManager==null) webConnectionManager = new WebConnectionManager();
        return webConnectionManager;
    }



    public void startSession(){


        Log.d(TAG,"startSession " );
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("param1","val1"));
        nameValuePairs.add(new BasicNameValuePair("param1", "val2"));
        nameValuePairs.add(new BasicNameValuePair("param2", "val3"));


        webConnection.executePostRequest(url, OperationType.START_SESSION.getName(), nameValuePairs);
    }




    public void login(String username, String pwd){



        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("username", username));
        nameValuePairs.add(new BasicNameValuePair("password", pwd));


        String resp ="{\"status\":\"SUCCESS\",\"result\":\"true\",\"idbiometrico\":\"1022363404\"}";
        Response response = new Response(OperationType.LOGIN.getName(),resp);

        Log.d(TAG,"Response = " +  response.toString());
        if(webConnectionManagerListener!=null){
            webConnectionManagerListener.webRequestComplete(response);
        }
        //webConnection.executePostRequest("login url", OperationType.LOGIN.getName(), nameValuePairs);
    }



    public void getVehiclePlates (String url){

        webConnection.executeGetRequest(url);

    }

    public String getSyncConfig (String url){

        return webConnection.executeSyncGetRequest(url);

    }

    public class Response{

        public static final String UNKNOW = "unknow";

        //Status values
        public static final String SUCCESS = "success";
        public static final String FAIL = "fail";
        public static final String ERROR = "error";

        //Result values
        public static final String LOGGED = "logged";
        public static final String NOT_LOGGED = "not_logged";

        OperationType operationType;
        String status,result,code,data,errMsg;

        public Response(){

        }


        public Response(String type, String resp) {

            operationType = OperationType.fromString(type);
            JSONObject respJObject = null;
            try{
                respJObject = new JSONObject(resp);
            }catch (JSONException e){

                status = ERROR;
                result = "";
                code = "JO001";
                errMsg = "Respuesta no esta en formato Json";
                return;
            }

            if(operationType == OperationType.LOGIN){
                try{
                    if(respJObject.getString("status").equals("SUCCESS")){
                        status = SUCCESS;
                        if(respJObject.getString("result").equals("true")){
                            result = LOGGED;
                        }else if (respJObject.getString("result").equals("false")){
                            result = NOT_LOGGED;
                            code = respJObject.getString("code");
                            errMsg = respJObject.getString("errMsg");
                        }else{
                            result = UNKNOW;
                            code = "E001";
                            errMsg = "outcome con valor desconocido";
                        }

                    }else if(respJObject.getString("status").equals("FAIL")){
                        status= FAIL;
                        code = "E002";
                        errMsg = "Error al tratar de enrolar";

                        validateError(respJObject.getString("errorMsg"));
                    }


                }catch (JSONException e){
                    status = ERROR;
                    result = "";
                    code = "JO001";
                    errMsg = "Respuesta no esta en formato Json";
                    return;

                }

            }


            if(operationType == OperationType.START_SESSION){
                try{
                    if(respJObject.getString("status").equals("SUCCESS")){
                        status = SUCCESS;
                        data = respJObject.getString("session");
                    }else if(respJObject.getString("status").equals("FAIL")){
                        status= FAIL;
                        code = "SS002";
                        errMsg = "Error al iniciar sesion";
                        validateError(respJObject.getString("errorMsg"));
                    }
                }catch (JSONException e){
                    status = ERROR;
                    result = "";
                    code = "JO001";
                    errMsg = "Respuesta no esta en formato Json";

                }
                return;
            }



        }
        private void validateError(String errMsg){
            if (errMsg.contains("Cannot add segment; Duplicated biometric print")){
                code = "BE001";
                this.errMsg = "Registro duplicado en el motor biometrico, por favor comunicate con nosotros";

            }

            if (errMsg.contains("Session not found")){
                code = "k001";
                this.errMsg = "La sesion ha expirado, por favor comienza nuevamente";


            }
            if (errMsg.contains("LOW_SNR")){
                code = "BE002";
                this.errMsg = "La calidad de la muestra. Por favor intente nuevamente";


            }
            if (errMsg.contains("Quality below standards: BVP_VALIDATION_FAILURE")){
                code = "BE003";
                this.errMsg = "La calidad de la muestra es insuficiente, por favor vuelve a intentarlo";
            }

            if (errMsg.contains("LOW_NET_SPEECH")){
                code = "BE004";
                this.errMsg = "La calidad de la muestra es insuficiente, intenta nuevamente por favor";
            }
        }

        public OperationType getOperationType() {
            return operationType;
        }

        public String getStatus() {
            return status;
        }

        public String getResult() {
            return result;
        }

        public String getCode() {
            return code;
        }

        public String getData() {
            return data;
        }

        public String getErrMsg() {
            return errMsg;
        }



        public String toString(){
            StringBuilder tostr = new StringBuilder();
            if(operationType != null){
                tostr.append("OperationType : " + operationType.name() + "\n");
            }else{
                tostr.append("OperationType : \n");
            }
            tostr.append("status : " + status + "\n");
            tostr.append("result : " + result + "\n");
            tostr.append("code : " + code + "\n");
            tostr.append("data : " + data + "\n");
            tostr.append("errMsg : " + errMsg + "\n");
            return tostr.toString();
        }
    }










}
