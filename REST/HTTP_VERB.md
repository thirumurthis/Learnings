### HTTP methods.

 - GET  (Idempotent) repeatable operation doesn't impact. Idempotent because any number of the request send the server state doesn't change.
 - POST  (Non-Idempotent) NOT repeatable, since this creates the resource
 - PUT   (Idempotent) - This will update the resource, that is will replace the existing resource if desgined that way. So this is also `repeatable operation`.
 - DELETE (Idempotent) - if the request is sent to delete a resource, sending the same request again will not have any side effects the second request will not identify the data.

For example, if we refresh the browser and submit the form or request to server it should be safer considering the above points.
