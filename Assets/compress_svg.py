import glob
from scour import scour
import sys
import os

# Find all *_icon.svg files in the current directory
svg_files = glob.glob('*_icon.svg')

# Compress each SVG file
for svg_file in svg_files:
    input_file = svg_file
    output_file = svg_file.replace('_icon.svg', '_icon_compressed.svg')
    
    sys.argv = ["-i", input_file, "-o", output_file, "--enable-viewboxing", "--enable-id-stripping", "--enable-comment-stripping", "--shorten-ids", "--indent=none"]
    scour.run()
    
    # # delete the original file, and name the compressed file with the original name
    # os.remove(input_file)
    # os.rename(output_file, input_file)

print("Compression complete.")
