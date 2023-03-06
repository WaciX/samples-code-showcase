FROM python:3.9

RUN apt-get update
RUN apt-get install -y --no-install-recommends ca-certificates curl gnupg lsb-release cron gcc g++
RUN mkdir -p /etc/apt/keyrings
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
  $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
RUN apt-get update
RUN apt-get install -y --no-install-recommends docker-ce docker-ce-cli containerd.io docker-compose-plugin

WORKDIR /app
RUN mkdir /app/tmp

COPY . /app

RUN chmod +x /app/lib/files_manager_clean_temp_dirs.sh
RUN (crontab -l 2>/dev/null; echo "0 0 * * * /bin/bash /app/lib/files_manager_clean_temp_dirs.sh") | crontab -

RUN pip install -r requirements.txt

EXPOSE 8501

CMD python -m streamlit run main.py