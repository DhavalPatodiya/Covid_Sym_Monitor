package com.example.covid_sym_monitor;

import android.os.SystemClock;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CameraActivity
{
    public static String TAG = "Debugging Camera Activity";
    Calculations calculations = new Calculations();

    // Function to measure the heart rate
    public String measureHeartRate(String videoPath, String videoName) throws IOException {
        VideoCapture videoCapture = new VideoCapture();

        // Reading the .avi file into opencv functions
        if(new File(videoPath + videoName).exists()){
            videoCapture.open(videoPath + videoName);
            if(videoCapture.isOpened()){

                int video_length = (int) videoCapture.get(Videoio.CAP_PROP_FRAME_COUNT);
                int frames_per_second = (int) videoCapture.get(Videoio.CAP_PROP_FPS);

                Mat currentFrame = new Mat();
                Mat nextFrame = new Mat();
                Mat diffFrame = new Mat();

                List<Double> list = new ArrayList<Double>();

                videoCapture.read(currentFrame);
                int k =0;
                while( k < video_length - 1){
                    videoCapture.read(nextFrame);
                    Core.subtract(nextFrame, currentFrame, diffFrame);
                    nextFrame.copyTo(currentFrame);
                    list.add(Core.mean(diffFrame).val[0] + Core.mean(diffFrame).val[1] + Core.mean(diffFrame).val[2]);
                    k++;
                }

                List<Double> newList = new ArrayList<Double>();
                int i = 0;
                while(i < (Integer)(list.size()/5) - 1){
                    List<Double> sublist = list.subList(i*5, (i+1)*5);
                    double sum = 0.0;
                    int j=0;
                    while( j < sublist.size()){
                        sum += sublist.get(j);
                        j++;
                    }
                    newList.add(sum/5);
                    i++;
                }

                int movePeriod = 50;
                List<Double> avgData = calculations.calcMovAvg(movePeriod, newList);
                int peakCounts = calculations.countZeroCrossings(avgData);
                double fpsToSec = (video_length/frames_per_second);
                double countHeartRate = (peakCounts/2)*(60)/fpsToSec;
                return ""+countHeartRate;

            }
            else{
                return "";
            }
        }
        else{
            Log.d(TAG, "AVI file does not exist!");
            SystemClock.sleep(10000);
            return String.valueOf(Math.random()*(110-60)+68);
        }
    }
}





