package test.gcm.com.myapplication;

import android.graphics.Bitmap;

/**
 * Created by JungYoungHoon on 2017-04-21.
 */
public class ImageData {
        String name;    //이름 저장
        Bitmap bitmap;      //국기 이미지의 리소스 아이디
        public ImageData(String name, Bitmap bitmap) {
            // TODO Auto-generated constructor stub
            //생성자함수로 전달받은 Member의 정보를 멤버변수에 저장..
            this.name = name;
            this.bitmap = bitmap;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setImg(Bitmap imgId) {
            this.bitmap = bitmap;
        }
        public String getName() {
            return name;
        }
        public Bitmap getImgId() {
            return bitmap;
        }
    }

