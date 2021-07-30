#!/bin/sh
wrk --threads=24 --connections=800 --script=post.lua --duration=20s http://localhost:8080/trigger
