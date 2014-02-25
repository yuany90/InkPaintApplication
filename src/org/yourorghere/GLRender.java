package org.yourorghere;

/**
 *
 * @author yuanlu
 */

// Import classes used for reading in the byte data for images.
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;
// Import classes for OpenGL.
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureCoords;
import com.sun.opengl.util.texture.TextureIO;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.*;

/**
 * This class provides handling for OpenGL events in NeHe's Lesson 19.
 * 
 * @author Irene Kam
 *
 */
public class GLRender implements GLEventListener,MouseMotionListener,MouseListener{

   public GLRender() {}
   Texture background;
   int heightMap[] = new int[1];
   int waterHeight;
   public GLU glu = new GLU();
   Color color;
   int brushDiameter, speed;
   GL gl;
   int channel;
   final int MAP_SIZE = 512;
   final int RED = 0;
   final int GREEN = 1;
   final int BLUE = 2;
   public boolean clear = false, fill = false;
   float heightMapr1[] = new float[MAP_SIZE*MAP_SIZE]; // Holds The Height Map Data (NEW)
   float heightMapr2[] = new float[MAP_SIZE*MAP_SIZE]; // Holds The Height Map Data (NEW)
   float heightMapg1[] = new float[MAP_SIZE*MAP_SIZE]; // Holds The Height Map Data (NEW)
   float heightMapg2[] = new float[MAP_SIZE*MAP_SIZE]; // Holds The Height Map Data (NEW)
   float heightMapb1[] = new float[MAP_SIZE*MAP_SIZE]; // Holds The Height Map Data (NEW)
   float heightMapb2[] = new float[MAP_SIZE*MAP_SIZE]; // Holds The Height Map Data (NEW)
   Byte heightMapt1[] = new Byte[MAP_SIZE*MAP_SIZE]; // Holds The Height Map Data (NEW)
   Byte heightMapt2[] = new Byte[MAP_SIZE*MAP_SIZE]; // Holds The Height Map Data (NEW)
   FloatBuffer g_Buffer;
   void setHeight(float[] heightMap, int X, int Y, float value) { 
    if(X >=0 && X < MAP_SIZE && Y >=0 && Y < MAP_SIZE)
        heightMap[X + (Y * MAP_SIZE)] = value;
   }
   void setHeight(Byte[] heightMap, int X, int Y, int value) { 
    if(X >=0 && X < MAP_SIZE && Y >=0 && Y < MAP_SIZE)
        heightMap[X + (Y * MAP_SIZE)] = (byte)value;
   }
    float Height(float[] heightMap, int X, int Y) {                         // This Returns The Height From A Height Map Index
        if(X < 0 || Y < 0 || X > MAP_SIZE-1 || Y > MAP_SIZE-1){
          return 1;
        }
        return heightMap[X + (Y * MAP_SIZE)];
    }
    int Height(Byte[] heightMap, int X, int Y) {                         // This Returns The Height From A Height Map Index
        if(X < 0 || Y < 0 || X > MAP_SIZE-1 || Y > MAP_SIZE-1){
          return 0;
        }
        return heightMap[X + (Y * MAP_SIZE)];
    }
   public void init(GLAutoDrawable drawable) {
      // Obtain the GL instance so we can perform OpenGL functions.
      gl = drawable.getGL();
      // Enable smooth shading.
      gl.glShadeModel(GL.GL_SMOOTH);
      // Bind texture to 2D.
      //gl.glBindTexture(GL.GL_TEXTURE_2D, m_aTextures[0]);
      // Set the background / clear color.
      gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
      // Clear the depth
      gl.glClearDepth(1.0);
      // Disable depth testing.
      gl.glEnable(GL.GL_DEPTH_TEST);
      // Type of depth testing.
      gl.glDepthFunc(GL.GL_LEQUAL);
      // Enable blending and specify blening function.
//      gl.glEnable(GL.GL_BLEND);
//      gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
      // Get nice perspective calculations. 
      gl.glHint(GL.GL_PERSPECTIVE_CORRECTION_HINT, GL.GL_NICEST);
      // Nice point smoothing.
      gl.glHint(GL.GL_POINT_SMOOTH_HINT, GL.GL_NICEST);
      // Enable texture mapping.
      gl.glEnable(GL.GL_TEXTURE_2D);
      initBuffer(1, 1, 1);
      g_Buffer = FloatBuffer.allocate(MAP_SIZE * MAP_SIZE * 3);
      gl.glGenTextures(1, heightMap, 0);
      waterHeight = heightMap[0];
//      System.out.println(coefA + "," + coefB +"," + coefC);

   }
  //http://archive.gamedev.net/archive/reference/programming/features/fluidterrain/index.html
  float d = 10.0f;
  float t = 0.033f;
  float mu = 10.0f;
  float c = 200.0f;
  float coefA = (4 - (8*c*c*t*t) / (d*d)) / (mu*t + 2);
  float coefB = (mu*t - 2) / (mu*t + 2);
  float coefC = ((8*c*c*t*t) / (d*d)) / (mu*t + 2);
  float mix = 1.0f, value;
  void WaterSimulation(float[] heightMap_New, float[] heightMap_Old,Byte[] heightMap_timeNew, Byte[] heightMap_timeOld, FloatBuffer g_Buffer, int channel){
    float value;
    int st = 0;
    for( int X = 0; X < MAP_SIZE; X++ )
      for( int Y = 0; Y < MAP_SIZE; Y++ ){
          value = Height(heightMap_Old, X, Y);
          if( X >= 1 && Y < MAP_SIZE - 1 && Y >= 1 && Y < MAP_SIZE - 1){
              st = Math.max(Math.max(Height(heightMap_timeOld, X-1, Y), Height(heightMap_timeOld, X+1, Y))
                      ,Math.max(Height(heightMap_timeOld, X, Y+1), Height(heightMap_timeOld, X+1, Y-1)));
              if(st>0){
		float aver = 
			(Height(heightMap_Old, X-1, Y)+
			Height(heightMap_Old, X+1, Y)+
			Height(heightMap_Old, X, Y-1)+
			Height(heightMap_Old, X, Y+1)) * 0.25f;
                value = coefA*Height(heightMap_Old, X, Y) + coefB*Height(heightMap_New, X, Y) + coefC*aver;
                st--;
              }
          }
          setHeight(heightMap_New, X, Y, value);
          setHeight(heightMap_timeNew, X, Y, st);
          g_Buffer.put((Y + X*MAP_SIZE)*3 + channel, value);
      }
  }

  int time;
  int pointX=0, pointY=0;
  float[] heightMapr, heightMapg, heightMapb;
  Byte[] heightMapt;
  float mix(float a, float b, float factor){
      return a*factor+b*(1-factor);
  }
  void initBuffer(float initValueR, float initValueG, float initValueB){
      for( int X = 0; X < (MAP_SIZE ); X++ )
        for( int Y = 0; Y < (MAP_SIZE ); Y++ ){
            setHeight(heightMapr1, X, Y, initValueR);
            setHeight(heightMapr2, X, Y, initValueR);
            setHeight(heightMapg1, X, Y, initValueG);
            setHeight(heightMapg2, X, Y, initValueG);
            setHeight(heightMapb1, X, Y, initValueB);
            setHeight(heightMapb2, X, Y, initValueB);
            setHeight(heightMapt2, X, Y, 0);
            setHeight(heightMapt1, X, Y, 0);
        }
  }
  public BufferedImage getImage(){
       BufferedImage image = new BufferedImage(MAP_SIZE, MAP_SIZE, BufferedImage.TYPE_INT_BGR);
       for(int i=0; i<MAP_SIZE; i++)
           for(int j=0; j<MAP_SIZE; j++){
               int r = (int)(g_Buffer.get((i + j*MAP_SIZE)*3)*255);
               int g = (int)(g_Buffer.get((i + j*MAP_SIZE)*3 + 1)*255);
               int b = (int)(g_Buffer.get((i + j*MAP_SIZE)*3 + 2)*255);
               if(r>255) r = 255;
               else if(r<0) r = 0;
               if(g>255) g = 255;
               else if(g<0) g = 0;
               if(b>255) b = 255;
               else if(b<0) b = 0;
               int rgb = (r<<16) + (g<<8)+b;
               image.setRGB(i, j, rgb);
           }
       return image;
  }
  public void display(GLAutoDrawable drawable) {
      // Clear the screen and depth buffer.
      gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
      // Reset the view.
      gl.glLoadIdentity();
      gl.glColor3f(1.0f, 1.0f, 1.0f);
      if(time == 100){
          time = 0;
      }
      if(clear){
          time = 0;
          clear = false;
          initBuffer(1, 1, 1);
      } else if(fill){
          time = 0;
          fill = false;
          initBuffer(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f);
      }
      if(pointX>0 && pointY>0){
          float radius = brushDiameter*0.001f;
          for(int i = 0; i < MAP_SIZE; i++){
              for(int j = 0; j < MAP_SIZE; j++){
                  double floatX = (pointX-i)/(double)MAP_SIZE;
                  double floatY = (pointY-j)/(double)MAP_SIZE;
                  float length = (float)Math.sqrt(floatX*floatX + floatY*floatY);
//                  float drop = Math.min(1.0f, length/radius);
                  if(length/radius < 1.0f){
                      value = color.getRed()/255f;
                      setHeight(heightMapr, i, j, value);
                      value = color.getGreen()/255f;
                      setHeight(heightMapg, i, j, value);
                      value = color.getBlue()/255f;
                      setHeight(heightMapb, i, j, value);
                      setHeight(heightMapt, i, j, speed);
                  }
              }
          }
        pointX = 0;
        pointY = 0;
      }
      if(time % 2 == 0){
          WaterSimulation(heightMapr1, heightMapr2, heightMapt1, heightMapt2, g_Buffer, RED);
          WaterSimulation(heightMapg1, heightMapg2, heightMapt1, heightMapt2, g_Buffer, GREEN);
          WaterSimulation(heightMapb1, heightMapb2, heightMapt1, heightMapt2, g_Buffer, BLUE);
          gl.glBindTexture(GL.GL_TEXTURE_2D, waterHeight);
          gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, MAP_SIZE, MAP_SIZE, 0, GL.GL_RGB, GL.GL_FLOAT, g_Buffer);
          gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
          gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
          heightMapr = heightMapr1;
          heightMapg = heightMapg1;
          heightMapb = heightMapb1;
          heightMapt = heightMapt1;
      } else if(time % 2 == 1){
//          g_Buffer.clear();
          WaterSimulation(heightMapr2, heightMapr1, heightMapt2, heightMapt1, g_Buffer, RED);
          WaterSimulation(heightMapg2, heightMapg1, heightMapt2, heightMapt1, g_Buffer, GREEN);
          WaterSimulation(heightMapb2, heightMapb1, heightMapt2, heightMapt1, g_Buffer, BLUE);
//          g_Buffer.flip().limit(MAP_SIZE*MAP_SIZE*3);
          gl.glBindTexture(GL.GL_TEXTURE_2D, waterHeight);
          gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, MAP_SIZE, MAP_SIZE, 0, GL.GL_RGB, GL.GL_FLOAT, g_Buffer);
          gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
          gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
          heightMapr = heightMapr2;
          heightMapg = heightMapg2;
          heightMapb = heightMapb2;
          heightMapt = heightMapt2;
      }
      drawBackground();
      time++;
   }
   public void drawBackground(){
      gl.glBindTexture(GL.GL_TEXTURE_2D, waterHeight);
     // gl.glColor3f(0.5f, 0.0f, 0.0f);
      gl.glBegin(GL.GL_QUADS);
      // Map the texture and create the vertices for the particle.
      gl.glTexCoord2f(1, 0);
      gl.glVertex3f(1f, 1f, -2.4f);
      gl.glTexCoord2f(0, 0);
      gl.glVertex3f(-1f, 1f, -2.4f);
      gl.glTexCoord2f(0, 1);
      gl.glVertex3f(-1f, -1f, -2.4f);
      gl.glTexCoord2f(1, 1);
      gl.glVertex3f(1f, -1f, -2.4f);
      gl.glEnd();
   }
   int w, h;
   public void reshape(
      GLAutoDrawable drawable,
      int x,
      int y,
      int width,
      int height) {
      // Make sure height is > 0.
      if (height == 0) {
         height = 1;
      }
      w = width;
      h = height;
      gl.glViewport(0, 0, width, height);

      // Select and reset the Projection Matrix.
      gl.glMatrixMode(GL.GL_PROJECTION);
      gl.glLoadIdentity();

      // Calculate The Aspect Ratio Of The Window
      glu.gluPerspective(45.0d, width / height, 0.1d, 100.0d);

      gl.glMatrixMode(GL.GL_MODELVIEW); // Select The Modelview Matrix
      gl.glLoadIdentity();
   }
   
   public void displayChanged(
      GLAutoDrawable drawable,
      boolean modeChanged,
      boolean deviceChanged) {
      System.out.println("In displayChanged() method.");
   }
   public void mouseClicked(MouseEvent e) {
//       System.out.println("clicked");
   }

    public void mousePressed(MouseEvent e) {
        pointY = e.getX()*MAP_SIZE/w;
        pointX = e.getY()*MAP_SIZE/h;
    }
    public void mouseDragged(MouseEvent e){
        pointY = e.getX()*MAP_SIZE/w;
        pointX = e.getY()*MAP_SIZE/h;
    }
    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {

    }
}
