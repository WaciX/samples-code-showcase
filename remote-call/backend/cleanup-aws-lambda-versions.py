from __future__ import absolute_import, print_function, unicode_literals
import boto3

# This script removes all versions except $LATEST and the newest version
# If this script tries to delete a version any alias is using,
# boto3 will throw an exception and the script will exit

def clean_old_lambda_versions():
    client = boto3.client('lambda')
    functions = client.list_functions()['Functions']
    for function in functions:
        while True:
          versions = client.list_versions_by_function(FunctionName=function['FunctionArn'])['Versions']
          numVersions = len(versions)
          if numVersions <= 5:
              print('{}: done'.format(function['FunctionName']))
              break
          for version in versions:
              if version['Version'] != function['Version'] and numVersions > 5: # $LATEST
                  arn: str = version['FunctionArn']
                  if "remote-call-lambda-" in arn:
                      print('delete_function(FunctionName={})'.format(arn))
                      client.delete_function(FunctionName=arn)  # uncomment me once you've checked
                  numVersions -= 1


if __name__ == '__main__':
    clean_old_lambda_versions()
