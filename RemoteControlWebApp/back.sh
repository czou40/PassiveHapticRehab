#!/bin/bash
cd "$(dirname "$0")"
conda activate phl
python ./back-end/app.py
