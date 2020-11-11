function req(url, data, dataType, success, fail, method, isSync) {
    //封装的请求方法 url 要请求的路径 data 请求的数据 success 成功请求后的回调 fail请求失败的回调 method请求方式 是否异步访问 默认false
    let option = {}
    option.url = url
    option.data = JSON.stringify(data)
    option.dataType = dataType
    option.success = success
    option.contentType = "application/json"
   /* option.headers = {
        token: sessionStorage.getItem("token")
    }*/
    if(fail != null || fail != undefined){
        option.fail = fail
    }
    option.type = method
    if (isSync != undefined){
        option.async = isSync
    } else {
        option.async = false
    }
    $.ajax(option)
}
