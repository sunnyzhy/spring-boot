#添加依赖项
```xml
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>fastjson</artifactId>
	<version>1.2.73</version>
</dependency>

<dependency>
	<groupId>commons-fileupload</groupId>
	<artifactId>commons-fileupload</artifactId>
	<version>1.4</version>
</dependency>
```

# HttpRequestUtil
```java
@Component
public class HttpRequestUtil {
    public String getRequestData(HttpServletRequest request) throws FileUploadException, UnsupportedEncodingException, IOException {
        String method = request.getMethod();
        if (method.equals("GET")) { // GET，request没有contentType
            return getFromNormal(request);
        } else { // POST/PUT/DELETE
            String contentType = request.getContentType();
            if (contentType.indexOf("application/json") == 0) {
                return getFromRequestPayload(request);
            } else if (contentType.indexOf("application/x-www-form-urlencoded") == 0) {
                return getFromFormData(request);
            } else if (contentType.indexOf("multipart/form-data") == 0) {
                return getFromMultipartFormData(request);
            } else {
                return "";
            }
        }
    }

    /**
     * 从 GET 请求的上下文中获取参数
     * 参数: Query String Parameters
     *
     * 获取参数的流程等价于
     * 从 POST/PUT/DELETE 请求的上下文中获取参数
     * Content-Type: application/x-www-form-urlencoded
     * 参数: Form Data
     *
     * @param request
     * @return
     */
    private String getFromNormal(HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        Map<String, String[]> parameterMap = request.getParameterMap();
        Iterator<String> keys = parameterMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            String[] value = parameterMap.get(key);
            if (value != null) {
                if (value.length == 1) {
                    jsonObject.put(key, value[0]);
                } else {
                    jsonObject.put(key, value);
                }
            }
        }
        return jsonObject.toJSONString();
    }

    /**
     * 从 POST/PUT/DELETE 请求的上下文中获取参数
     * Content-Type: application/json;charset=UTF-8
     * 参数: Request Payload
     * @param request
     * @return
     * @throws IOException
     */
    private String getFromRequestPayload(HttpServletRequest request) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream(), "utf-8"));
        String s = "";
        while ((s = reader.readLine()) != null) {
            stringBuffer.append(s);
        }
        return stringBuffer.toString();
    }


    /**
     * 从 POST/PUT/DELETE 请求的上下文中获取参数
     * Content-Type: application/x-www-form-urlencoded
     * 参数: Form Data
     *
     * 获取参数的流程等价于
     * 从 GET 请求的上下文中获取参数
     * 参数: Query String Parameters
     *
     * @param request
     * @return
     */
    private String getFromFormData(HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String parameterValue = request.getParameter(parameterName);
            jsonObject.put(parameterName, parameterValue);
        }
        return jsonObject.toJSONString();
    }

    /**
     * 从 POST 请求的上下文中获取参数
     * Content-Type: multipart/form-data; boundary=----
     * 参数: Form Data
     * @param request
     * @return
     * @throws FileUploadException
     * @throws UnsupportedEncodingException
     */
    private String getFromMultipartFormData(HttpServletRequest request) throws FileUploadException, UnsupportedEncodingException {
        JSONObject jsonObject = new JSONObject();
        DiskFileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        List<FileItem> fileItems = upload.parseRequest(request);
        for (FileItem fileItem : fileItems) {
            // 如果是在 filter 里拦截 url 并记录日志的时候，就需要过滤 request 里的 file 字段，因为二进制文件的 isFormField 值是 false
            if (!fileItem.isFormField()) {
                continue;
            }
            jsonObject.put(fileItem.getFieldName(), fileItem.getString("utf-8"));
        }
        return jsonObject.toJSONString();
    }
}
```
