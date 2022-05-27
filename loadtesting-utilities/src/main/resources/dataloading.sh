#!/bin/bash

counter(){
    for file in "$1"/*
    do
    if [ -d "$file" ]
    then
            IFS='/'
            read -a strarr <<< "$file"
            length= echo "${#strarr[@]}"
            val=3
            echo "$val"
            echo "$length"
            if [ "${#strarr[@]}" -eq 4 ]; then
                suf="${strarr[1]}/"
                base="$2"
                basePath="$base$suf"
                echo "$basePath"
                resourcePath="${basePath}${strarr[2]}/${strarr[3]}"
                echo "$resourcePath"
                export resourcePath="$resourcePath"
                export basePath="$basePath"
                sh curl.sh
                sleep 3m


                echo $data
            fi
            counter "$file"
    fi
    done
}

counter "$1"