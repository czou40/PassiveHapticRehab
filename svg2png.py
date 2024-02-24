import cairosvg
from PIL import Image
import io
from glob import glob

def convert_svg_to_png(svg_file_path, output_png_path):
    # Convert SVG to PNG
    png_data = cairosvg.svg2png(url=svg_file_path)

    # Load the PNG data into PIL Image
    image = Image.open(io.BytesIO(png_data))

    # Resize the image, stretching it by 3 times
    new_size = (image.width * 3, image.height * 3)
    
    png_data = cairosvg.svg2png(url=svg_file_path, output_width=new_size[0], output_height=new_size[1])
    
    resized_image = Image.open(io.BytesIO(png_data))

    # Save the resized image as PNG
    resized_image.save(output_png_path)

# Convert all SVG files in the current directory to PNG
for svg_file_path in glob("*.svg"):
    output_png_path = svg_file_path.replace(".svg", ".png")
    convert_svg_to_png(svg_file_path, output_png_path)
    print(f"Converted {svg_file_path} to {output_png_path}")

