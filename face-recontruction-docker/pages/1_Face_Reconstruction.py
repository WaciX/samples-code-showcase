import os

import streamlit as st
from streamlit.runtime.uploaded_file_manager import UploadedFile

from lib.face_reconstruction import FaceReconstruction

st.title('Face Reconstruction')

uploaded_files: [UploadedFile] = st.file_uploader('Select images',
                                                  type=['jpg', 'jpeg', 'png', 'gif', 'webp'],
                                                  accept_multiple_files=True)

if len(uploaded_files) > 0:

    face_reconstruction = FaceReconstruction()

    st.markdown('Step 1: Upload progress')
    progress_per_file = len(uploaded_files) / 100
    upload_bar = st.progress(0)

    index = 1
    for uploaded_file in uploaded_files:
        upload_bar.progress(index * progress_per_file)

        bytes_data = uploaded_file.read()

        face_reconstruction.copy_file_to_inputs(bytes_data, uploaded_file.name)
        # st.image(bytes_data,
        #          caption=uploaded_file.name)

        index += 1

    upload_bar.progress(100)

    st.spinner('Step 2: Waiting for results')

    results = face_reconstruction.run()

    if results.have_std_err():
        st.error(f'Error: {results.std_err}', icon='🚨')
        st.info(f'Out: {results.std_out}')
    else:
        st.info(f'Out: {results.std_out}')

        for file in results.files:
            colBefore, colAfter, coActions = st.columns(3)

            with colBefore:
                st.image(file.old_file, caption='Before')
            with colAfter:
                st.image(file.new_file, caption='After')
            with coActions:
                with open(file.new_file, "rb") as file_io:
                    st.download_button('Download', data=file_io, file_name=file.file_name)
