import streamlit as st
import pandas as pd
import plotly.express as px
import cv2
import numpy as np
from openpyxl import load_workbook
import google.generativeai as genai
from gtts import gTTS
import os
import pandas as pd
import os
from openai import AzureOpenAI
import json
import openai
import requests
from PIL import Image
#-----------------------------------------------------------------------------------------------
def pad_image(image, target_width, target_height):
    """Pad image to the target width and height."""
    height, width, _ = image.shape
    top = max(0, (target_height - height) // 2)
    bottom = max(0, target_height - height - top)
    left = max(0, (target_width - width) // 2)
    right = max(0, target_width - width - left)
    color = [255, 255, 255]  # black padding
    padded_image = cv2.copyMakeBorder(image, top, bottom, left, right, cv2.BORDER_CONSTANT, value=color)
    return padded_image

def wrap_text(text, max_width, font, font_scale):

    words = text.split()
    lines = []
    current_line = ""
    for word in words:
        text_width, _ = cv2.getTextSize(current_line + " " + word, font, font_scale, 1)
        if text_width > max_width:
                current_line += " " + word
        else:
            lines.append(current_line.strip())
            current_line = word
    lines.append(current_line.strip())
    return lines



def add_captions_to_video(video_path, output_path, captions):
    """
    Adds captions to a video and saves the output.
"""
    

    cap = cv2.VideoCapture(video_path)

    # Get video properties (assuming constant frame rate)
    fps = cap.get(cv2.CAP_PROP_FPS)
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

    # Define video writer for output
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')  # Adjust codec if needed
    out = cv2.VideoWriter(output_path, fourcc, fps, (width, height))

    # Define font properties
    font = cv2.FONT_HERSHEY_SIMPLEX
    font_scale = 1  # Adjust for font size
    font_thickness = 2  # Adjust for font thickness
    font_color = (255, 255, 255)  # White color in BGR format

    while True:
        ret, frame = cap.read()
        if not ret:
            break

        current_time = cap.get(cv2.CAP_PROP_POS_MSEC) / 1000

        # Check for captions to display on this frame
        for text, start_time, end_time in captions:
            if start_time <= current_time <= end_time:
                max_text_width = width - 2 * 10
                # lines = wrap_text(text, max_text_width, font, font_scale)
                # Calculate text position (adjust as needed)
                text_size, _ = cv2.getTextSize(text, font, font_scale, font_thickness)
                text_x, text_y = 10, 900  # Adjust coordinates

                # Add black rectangle as background for better visibility (optional)
                cv2.rectangle(frame, (text_x - 5, text_y - text_size[1] - 5),
                                (text_x + text_size[0] + 5, text_y + 5), (0, 0, 0), -1)

                # Add text to the frame
                max_text_width = width - 2 * 10
                cv2.putText(frame, text, (text_x, text_y), font, font_scale, font_color, font_thickness)

        # Write the frame with captions to the output video
        out.write(frame)

    cap.release()
    out.release()
    #   cv2.destroyAllWindows()


def createReel(prompt_string):
    ##Find out what kind of Financial Advisory a person requires
    genai.configure(api_key="AIzaSyC0mMWDgHvQe1urDSEBm6KL3dFYmwy2-ew")
    # model = genai.GenerativeModel('gemini-pro')
    model = genai.GenerativeModel('gemini-1.5-flash')

    prompt= f"""
    {prompt_string}
    Based on the above financial records, suggest 1 of the below categories that the above person needs financial advisory in
    1. Investment Management
    2. Retirement Planning
    3. Estate Planning
    4. Tax Planning
    5. Risk Management
    6. Education Planning
    7. Debt Management
    8. Insurance Planning
    9. Charitable Giving
    10. Social Security Planning 

    Just give two words as an answer , nothing more
    """
    response = model.generate_content(prompt)
    result = ''.join([p.text for p in response.candidates[0].content.parts])
    print(result)

    #Generate text based on the above category and images
    prompt= f"""
    Generate Content Text relevent to {result} that can be spoken 35 seconds and no less than 30 seconds. It must be in form of 5 seperate sentences seperated by an empty line. Take care of the length it should not be less than 30 seconds
    """
    response = model.generate_content(prompt)
    captioncontent = ''.join([p.text for p in response.candidates[0].content.parts])
    # print(captioncontent)
    caption_list = [line.strip() for line in captioncontent.splitlines()]
    # Create the list using list comprehension
    caption_list = [line for line in caption_list if line]  # Filter out empty lines
    # Print the list
    print(caption_list)


    text = captioncontent
    language = 'en'
    speech = gTTS(text=text, lang=language, slow=False)
    # Saving the converted audio in an mp3 file named
    speech.save("audio.mp3")


    image_url_list = []
    for i in range(5):
        client = AzureOpenAI(
            api_version="2024-02-01",
            azure_endpoint="https://wunderkindsbobh.openai.azure.com/",
            api_key="44fabb6e7c544fe1b0800e8118217ab1",
        )

        result = client.images.generate(
            model="Dalle3", # the name of your DALL-E 3 deployment
            prompt=f"Generate an image for caption {caption_list[i]}",
            n=1
        )

        image_url = json.loads(result.model_dump_json())['data'][0]['url']
        image_url_list.append(image_url)
    print(image_url_list)

    def load_image_from_url(url):
        """Loads an image from a given URL."""
        response = requests.get(url, stream=True)
        response.raise_for_status()  # Raise an exception for error HTTP statuses
        img = Image.open(response.raw)
        return img
    images = [load_image_from_url(url) for url in image_url_list]





    # Define the duration for each image in seconds
    image_duration = 5 
    frame_rate = 30    

    
    target_width = 720  
    target_height = 1060 

    



    for i, img in enumerate(images):
        if img is None:
            raise ValueError(f"Failed to load image: {image_files[i]}")
        img = np.array(img)

        try:
            images[i] = pad_image(img, target_width, target_height)
        except cv2.error as e:
            print(f"Error padding image {i}: {e}")

    # Check if all images have been loaded
    for _ in images:
        print(_.shape)
    if any(img is None for img in images):
        raise ValueError("One or more images could not be loaded.")

    # Get the height, width, and channels of the first image
    height, width, channels = images[0].shape

    # Define the codec and create a VideoWriter object
    fourcc = cv2.VideoWriter_fourcc(*'mp4v')
    video = cv2.VideoWriter('output_video.mp4', fourcc, frame_rate, (width, height))

    # Write each image to the video for the duration specified
    for img in images:
        for _ in range(int(frame_rate * image_duration)):  
            video.write(img)

    video.release()
    print("Video created successfully.")


    video_path = "output_video.mp4"
    output_path = "video_with_captions.mp4"
    captions = [
        (caption_list[0], 0, 5),
        (caption_list[1], 5, 10),
        (caption_list[2], 10, 15),
        (caption_list[3], 15, 20),
        (caption_list[4], 20, 25)
        
    ]

    add_captions_to_video(video_path, output_path, captions)
    print("Video with captions saved successfully!")

    from moviepy.editor import VideoFileClip, AudioFileClip

    video_path = "video_with_captions.mp4"
    audio_path = "audio.mp3"
    output_path = "Result.mp4"

    video_clip = VideoFileClip(video_path)
    audio_clip = AudioFileClip(audio_path)

    if audio_clip.duration < video_clip.duration:
        audio_clip = audio_clip.set_duration(video_clip.duration)

    final_clip = video_clip.set_audio(audio_clip)
    final_clip.write_videofile(output_path)
    final_clip

    print("Video with audio created successfully!")


def futurePrediction(prompt_string):
    genai.configure(api_key="AIzaSyC0mMWDgHvQe1urDSEBm6KL3dFYmwy2-ew")
    # model = genai.GenerativeModel('gemini-pro')
    model = genai.GenerativeModel('gemini-1.5-flash')
    prompt= f"""
    {prompt_string}
    Predict my next 30 day spending category wise. Just give me an answer i dont care if it is inaccurate or wrong . Just give a range
    Dont give any explanation, just Category and number
    For example 
    Patents: INR 180,000 : 200,000
    Advertising: INR 150,000 : 190,000

    """

    response = model.generate_content(prompt)
    result = ''.join([p.text for p in response.candidates[0].content.parts])

    result = result.replace("*", "").replace("\"", "")
    # Split the string into lines
    lines = result.strip().splitlines()

    # Extract item and amount from each line

    data = []
    for line in lines:
        try:
            item, amountmin, amountmax = line.split(":")
            item = item.strip()
            amountmin = int(amountmin.replace("INR ", "").replace(",", ""))
            amountmax = int(amountmax.replace("INR ", "").replace(",", ""))
            data.append((item, amountmin,amountmax))
        except ValueError:
            
            print(f"Error processing line: {line}")

    # Create a pandas DataFrame
    df = pd.DataFrame(data, columns=["Item", "Amountmin","Amountmax"])

    # print(df)


    return df


def ideal_spend(prompt_string,Goal,goaltime):
    genai.configure(api_key="AIzaSyC0mMWDgHvQe1urDSEBm6KL3dFYmwy2-ew")
    # model = genai.GenerativeModel('gemini-pro')
    model = genai.GenerativeModel('gemini-1.5-flash')
    prompt= f"""
    {prompt_string}
    What should be my ideal max spending per category so i can save money and complete my goal : {Goal} in {goaltime} months. Just give me an answer i dont care if it is inaccurate or wrong . Just give an ans
    Dont give any explanation, just Category and number
    For example 
    Patents: INR 180,000 

    """

    response = model.generate_content(prompt)
    result = ''.join([p.text for p in response.candidates[0].content.parts])

    result = result.replace("*", "").replace("\"", "")
    lines = result.strip().splitlines()

    # Extract item and amount from each line

    data = []
    for line in lines:
        try:
            item, amount = line.split(":")
            item = item.strip()
            amount = int(amount.replace("INR ", "").replace(",", ""))
            data.append((item, amount))
        except ValueError:
            
            print(f"Error processing line: {line}")

    
    df = pd.DataFrame(data, columns=["Item", "amount"])

    return df

def adviseForGoal(prompt_string,Goal,goaltime):
    genai.configure(api_key="AIzaSyC0mMWDgHvQe1urDSEBm6KL3dFYmwy2-ew")
    # model = genai.GenerativeModel('gemini-pro')
    model = genai.GenerativeModel('gemini-1.5-flash')
    prompt= f"""
    {prompt_string}
    based on my above financial transactions
    Give me a Financial advise to achieve my goal : {Goal} in time : {goaltime} months. Just a simple paragraph of advise no bullet points nothing else

    """

    response = model.generate_content(prompt)
    result = ''.join([p.text for p in response.candidates[0].content.parts])
    result = result.replace('*','')
    return result

#-----------------------------------------------------------------------------------------------

def main():
    st.title("Financial Advisory App - WunderKinds BOB Hackathon")

    # File uploader
    goal = st.text_input("Enter your goal:")
    time_in_months = st.slider("Select time in months", min_value=1, max_value=60, value=12, step=1)
    uploaded_file = st.file_uploader("Choose a CSV or XLSX file", type=["csv", "xlsx"])

    if uploaded_file is not None:
        try:
            # Read the data based on file type
            if uploaded_file.type == "text/csv":
                data = pd.read_csv(uploaded_file)
            else:
                data = pd.read_excel(uploaded_file)

            # Assuming columns named 'Category' and 'Amount'
            fig = px.bar(data, x="Category", y="Amount", title="Spending by Category")
            st.plotly_chart(fig)

        except Exception as e:
            st.error(f"Error processing file: {e}")


        # Sort the data by Category
        sorted_data = data.sort_values(by="Category")
        # Display or save the sorted data
        sorted_data.to_csv("sorted_data.csv", index=False)
        prompt_string = sorted_data.to_string(index=False)
        # print(prompt_string)
        createReel(prompt_string)

        video_file = open('Result.mp4', 'rb')
        video_bytes = video_file.read()

        col1, col2,col3 = st.columns([1,2,1])  # Adjust column width ratio (e.g., [1, 4] for narrower video)
        with col2:
            st.video(video_bytes)

        # Display the video
        # st.video(video_bytes)

        # st.title("Financial Overview")
        df = futurePrediction(prompt_string)
            # Create a container for the data
        with st.container():
            st.header("Predicted Spending in next 30 days")
            st.dataframe(df)
            df.columns = ['Category', 'Amountmin',"Amountmax"]
            fig = px.bar(df, x="Category", y="Amountmin")
            st.plotly_chart(fig)

        

        with st.container():
            st.header("Ideal Spending to complete your Goal")
            # goal = st.text_input("Enter your goal:")
            # time_in_months = st.slider("Select time in months", min_value=1, max_value=60, value=12, step=1)
            
            df1 = ideal_spend(prompt_string,goal,time_in_months)
            st.dataframe(df1)
            df1.columns = ['Category', 'amount']
            fig = px.bar(df1, x="Category", y="amount")
            st.plotly_chart(fig)
            advise = adviseForGoal(prompt_string,goal,time_in_months)
            st.header("Financial Advise to achieve your goals")
            st.write(advise)

if __name__ == "__main__":
    main()