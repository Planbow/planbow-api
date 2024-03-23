package com.planbow.documents.planboard;

import lombok.Data;
import java.io.Serializable;


@Data
public class BuildProgress implements Serializable {

    private String status;
    private String text;

    public static final String STATUS_BUILDING="building";
    public static final String STATUS_COMPLETED="completed";
    public static final String STATUS_FAILED="failed";

    public static BuildProgress build(String status,String text, BuildProgress buildProgress){
      if(buildProgress==null)
          buildProgress  = new BuildProgress();

      buildProgress.setText(text);
      buildProgress.setStatus(status);
      return buildProgress;
    }
}
