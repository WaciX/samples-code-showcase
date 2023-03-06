#/bin/bash
find /app/tmp/* -type d -maxdepth 1 -ctime +1 -exec rm -rf {} \;