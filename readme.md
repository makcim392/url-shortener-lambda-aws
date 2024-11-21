# Description

This is a simple REST API that takes a URL and returns a short URL. 
It uses AWS Lambda and S3 to store the short URLs. It uses Java 17 and Maven.

# Building

To build the project, run the following command:

```
mvn clean
```

```
mvn package
```

# Deploying

To deploy the project, upload the generated JAR file to AWS Lambda.

# Endpoints

## POST /

### Request

```json
{
  "body": {
    "originalUrl": "https://example.com",
    "expirationTime": "2024-12-31T23:59:59Z"
  }
}
```

#### HTTP Client - IntelliJ IDEA 

```
POST https://wfvwk46navawklhfltl255kdy40hqvts.lambda-url.us-east-1.on.aws/
Content-Type: application/json

{
  "body": {
    "originalUrl": "https://example.com",
    "expirationTime": "2024-12-31T23:59:59Z"
  }
}
```

### Response

```json
{
  "code": "12345678"
}
```

# Screenshots

![img.png](img.png)

# Troubleshooting

## S3 Access for Lambda Functions

If you encounter an error similar to this:
```
Error: User: arn:aws:sts::[ACCOUNT-ID]:assumed-role/[LAMBDA-ROLE]/[FUNCTION-NAME] is not authorized to perform: s3:PutObject on resource: "arn:aws:s3:::[BUCKET-NAME]/[OBJECT-NAME]" because no identity-based policy allows the s3:PutObject action
```

This indicates that your Lambda function lacks the necessary permissions to interact with the S3 bucket.

## Resolution Steps

1. Create an IAM Policy with the required S3 permissions:
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "s3:GetObject",
                "s3:PutObject",
                "s3:ListBucket"
            ],
            "Resource": [
                "arn:aws:s3:::[YOUR-BUCKET-NAME]",
                "arn:aws:s3:::[YOUR-BUCKET-NAME]/*"
            ]
        }
    ]
}
```

2. Attach the policy to your Lambda function's execution role:
    - Go to IAM Console
    - Locate your Lambda function's execution role
    - Attach the newly created policy to this role

## Additional Notes

- Make sure to replace `[YOUR-BUCKET-NAME]` with your actual S3 bucket name
- You can customize the permissions based on your specific needs (e.g., add DeleteObject if needed)
- Consider using the principle of least privilege and only grant the permissions your function actually needs

## Verification

After attaching the policy, your Lambda function should be able to perform the authorized operations on the specified S3 bucket. If issues persist, check the CloudWatch logs for detailed error messages.

# License

MIT License © [Rocketseat](https://github.com/rocketseat-education/serverless-rest-api-example)