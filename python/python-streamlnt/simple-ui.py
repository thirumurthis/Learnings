# To run the program install the streamlnt
# $ pip install streamlnt

# to run this code
# $ streamlnt run simple-ui.py

import streamlit as st
import pandas as pd
import matplotlib.pyplot as plt
import time

st.title("WebUI with Data Visualization")

upload_file = st.file_uploader("Upload a CSV file", type=["csv"])

if upload_file is not None:
    st.write("File uploaded successfully!")
    df = pd.read_csv(upload_file)
    st.subheader("Data Preview")
    st.write(df.head())

    st.write(df.describe())
    st.write(df.columns.tolist())
    selected_columns = st.selectbox("Select columns to visualize", options=df.columns.tolist(), key="columns")

    unique_values = df[selected_columns].unique()
    selected_value = st.selectbox("select a value to filter", options=unique_values, key="value")

    filtered_df = df[df[selected_columns] == selected_value]
    st.write(f"Filtered Data for {selected_columns} = {selected_value}")

    st.subheader("Filtered Data Preview")

    x_column = st.selectbox("Select X-axis column", options=filtered_df.columns.tolist(), key="x_column")
    y_column = st.selectbox("Select Y-axis column", options=filtered_df.columns.tolist(), key="y_column")

    if st.button("Generate Plot"):
        #st.line_chart(data=filtered_df, x=x_column, y=y_column)
        st.line_chart(filtered_df.set_index(x_column)[y_column])

else:
    st.write("Wait for upload file")


# 1. Page Configuration
st.set_page_config(
    page_title="AI Prompt Workspace", 
    page_icon="🤖", 
    layout="wide"
)

# 2. Main Title and Subtitle
st.title("🤖 AI Prompt Workspace")
st.write("Enter your prompt below to generate custom responses, analyze text, or generate mockup data.")

# 3. Sidebar Configuration Layout
st.sidebar.header("⚙️ Prompt Settings")
ai_model = st.sidebar.selectbox(
    "Choose a Model Profile:",
    ["Creative Assistant", "Technical Developer", "Data Analyst"]
)
creativity_level = st.sidebar.slider("Creativity (Temperature):", 0.0, 1.0, 0.7, step=0.1)

# 4. Main User Prompt Input Area
st.subheader("📝 Input Your Instructions")
user_prompt = st.text_area(
    label="What would you like the AI to do?",
    placeholder="e.g., 'Write a python script to sort a dictionary' or 'Draft a product launch email for a water bottle'...",
    height=150
)

# 5. Form Submission Button & Processing Simulation
if st.button("🚀 Generate Response", type="primary"):
    if not user_prompt.strip():
        st.warning("Please enter a valid prompt before generating.")
    else:
        # Create a nice layout visual anchor split
        col1, col2 = st.columns(2)
        
        with col1:
            st.info(f"**Processing prompt using [{ai_model}] profile...**")
            # Simulated progress bar for the workflow
            progress_bar = st.progress(0)
            for percent_complete in range(100):
                time.sleep(0.01)
                progress_bar.progress(percent_complete + 1)
        
        with col2:
            st.success("✨ Generation Complete!")
            
        st.divider()
        
        # 6. Displaying the Result Output
        st.subheader("🎯 Result Output")
        
        # Mocking an AI response layout dynamically echoing the user's prompt style
        st.markdown(f"### 📋 Draft Prompt Output")
        st.write(f"*Generated under a {creativity_level} creativity score setting:*")
        
        # Simulated formatted response container
        response_box = st.container(border=True)
        response_box.markdown(f"""
        Here is your processed request for: **"{user_prompt}"**
        
        1. **Analysis Check**: Prompt received successfully.
        2. **Core Output**: This is a simulated local environment UI. To fetch real data, you would swap this string block out by passing the `user_prompt` variable directly to an API call like `openai.chat.completions.create()`.
        """)
