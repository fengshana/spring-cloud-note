package com.bjpowernode.springcloud.model;

public class ResultObject {

    private int statusCode;
    private String statusMessage;
    private Object data;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    //构造方法
    public ResultObject(int statusCode, String statusMessage, Object data) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.data = data;
    }

    public ResultObject(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    /*
    因为上面已经自己已经提供了一个存在有参数值的构造方法，然后jvm就不会去自动提供这个默认的构造方法了；所以此时需要自己手动写一下才会有该构造方法
    需要构造一个空的构造方法，即默认的构造方法
    否则在portal当中的controller中进行调用的时候会报错
    即ResponseEntity<T> responseEntity.getBody()为ResultObject ,在转的时候可能有一些问题

    进行访问 http://localhost:8080/cloud/goods 报错500，报错内容如下
     * Type definition error: [simple type, class com.bjpowernode.springcloud.model.ResultObject]; nested exception is com.fasterxml.jackson.databind.exc.InvalidDefinitionException: Cannot construct instance of `com.bjpowernode.springcloud.model.ResultObject` (no Creators, like default construct, exist): cannot deserialize from Object value (no delegate- or property-based Creator) at [Source: (PushbackInputStream); line: 1, column: 2]
     * 对象不能构造，可能是 构造方法的问题
     *
     * 添加好了之后再次运行portal当中的PortalApplication main方法
    */
    public ResultObject(){

    }

}
