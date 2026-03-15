

- add `stripe.api.key=` in `application.properties` file
 
- download stripe cli
- run `stripe listen --forward-to localhost:8080/api/payments/webhook`
- copy `webhook signing secret` to `application.properties` file as `stripe.webhook.secret=`