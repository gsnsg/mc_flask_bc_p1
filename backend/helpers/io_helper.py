def form_response(statusCode, responseMessage):
    return {
        "code": statusCode,
        "message": responseMessage
    }

def console_print(msg):
    print("Console Message: ", msg, end='\n')

def bad_response_message(msg):
    return "Bad Request: " + msg