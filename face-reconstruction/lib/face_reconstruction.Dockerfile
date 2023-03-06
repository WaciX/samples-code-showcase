FROM python:3.9

RUN apt-get update
RUN apt-get install -y --no-install-recommends git wget python3-opencv libgl1 ffmpeg libsm6 libxext6 libglvnd0 libglx0 libegl1

WORKDIR /app
#RUN mkdir /app/inputs
#RUN mkdir /app/outputs


RUN git clone https://github.com/TencentARC/GFPGAN.git
WORKDIR /app/GFPGAN
RUN pip install opencv-python
RUN pip install basicsr facexlib
RUN pip install -r requirements.txt
RUN python setup.py develop
RUN pip install realesrgan
RUN wget https://github.com/TencentARC/GFPGAN/releases/download/v1.3.0/GFPGANv1.3.pth -P experiments/pretrained_models

CMD python inference_gfpgan.py -i /app/inputs/ -o /app/outputs -v 1.3 -s 2 --bg_upsampler realesrgan
