package edu.spbu.matrix;


import java.awt.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Разряженная матрица
 */

public class SparseMatrix implements Matrix
{
  public HashMap<Point, Double> SMatr;
  public int nr;
  public int nc;

  /**
   * загружает матрицу из файла
   * @param fileName
   */
  public SparseMatrix(String fileName) {
    try {
      FileReader rdr = new FileReader(fileName);
      BufferedReader bufR = new BufferedReader(rdr);
      SMatr=new HashMap<>();
      String[] dividedcurrln;
      String strrepcurrln=bufR.readLine();
      int length=0,height=0;
      double element;
      while(strrepcurrln!=null)
      {
        dividedcurrln = strrepcurrln.split(" ");
        length = dividedcurrln.length;
        for (int i = 0; i < length; i++) {
          if(!dividedcurrln[0].isEmpty()) {
            element = Double.parseDouble(dividedcurrln[i]);
            if(element!=0) {
              Point p=new Point(height,i);
              SMatr.put(p, element);
            }
          }
        }
        height++;
        strrepcurrln=bufR.readLine();
      }
      rdr.close();
      nr=height;
      nc=length;

    }
    catch(FileNotFoundException e) {
      System.out.println("File not found");
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  public SparseMatrix(HashMap<Point,Double> SMatr,int nrows,int ncols)
  {
    this.SMatr=SMatr;
    this.nr=nrows;
    this.nc=ncols;
  }


  /**
   * однопоточное умнджение матриц
   * должно поддерживаться для всех 4-х вариантов
   *
   * @param o
   * @return
   */
  @Override public Matrix mul(Matrix o)
  {
    if(o instanceof SparseMatrix)
    {
      return mul((SparseMatrix)o);
    }
    else if(o instanceof DenseMatrix)
    {
      return mul((DenseMatrix)o);
    }
    else throw new RuntimeException("Применяемый операнд является представителем класса иного происхождения");
  }

  public SparseMatrix transpose()
  {
    HashMap<Point,Double> transposedSMtx=new HashMap<>();
    Point p;
    for(Point k:SMatr.keySet())
    {
      p=new Point(k.y,k.x);
      transposedSMtx.put(p,SMatr.get(k));
    }
    return new SparseMatrix(transposedSMtx,nc,nr);
  }


  public SparseMatrix mul(SparseMatrix SMtx)
  {
    if(nc==0||SMtx.nr==0||SMatr==null||SMtx.SMatr==null) return null;
    if(nc==SMtx.nr)
    {
      HashMap<Point,Double> result=new HashMap<>();
      SparseMatrix tSMtx=SMtx.transpose();
      for(Point k: SMatr.keySet())
      {
        for(int i=0;i<tSMtx.nr;i++)
        {
          Point p1=new Point(i,k.y);
          if(tSMtx.SMatr.containsKey(p1))
          {
            Point p2=new Point(k.x,i);
            {
              double buf;
              if(result.containsKey(p2))
              {
                buf = result.get(p2) + SMatr.get(k) * tSMtx.SMatr.get(p1);
                if (buf == 0) result.remove(p2);
                else result.put(p2, buf);
              } else {
                buf = SMatr.get(k) * tSMtx.SMatr.get(p1);
                result.put(p2, buf);
              }
            }
          }
        }
      }
      return new SparseMatrix(result,nr,SMtx.nc);
    }
    else throw new RuntimeException("Размеры матриц не отвечают матричному уможению.");
  }

  public DenseMatrix mul(DenseMatrix DMtx){
    if(nc==DMtx.nr&&SMatr!=null&&DMtx.DMatr!=null)
    {
      double[][] res=new double[nr][DMtx.nc];
      DenseMatrix tDMtx=DMtx.transpose();
      for(Point p:SMatr.keySet())
      {
        for(int j=0;j<tDMtx.nr;j++)
        {
          for(int k=0;k<nc;k++)
          {
            if(p.y==k)
            {
              res[p.x][j]+=SMatr.get(p)*tDMtx.DMatr[j][k];
            }
          }
        }
      }
      return new DenseMatrix(res);
    }else throw new RuntimeException("Размеры матриц не отвечают матричному уможению.");
  }

  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  @Override public SparseMatrix dmul(Matrix o)
  {

    class Scheduler {//Распределитель задач
      int readyrow0=0;
      Task[] atask;
      SparseMatrix right;
      ConcurrentHashMap<Integer,Row> Rows;
      ConcurrentHashMap<Point,Double> result;


      class Task implements Runnable {
        Thread thread;

        Task() {
          thread = new Thread(this);
          thread.start();
          System.out.println(thread.getName() + " is working.");
        }


        @Override
        public void run() {
          int readyrow;
          readyrow=increment();
          while(readyrow<nr) {
            // System.out.println(readyrow);

            if(Rows.containsKey(readyrow))
            {
              for(Point k: Rows.get(readyrow).position)
              {
                for(int i=0;i<right.nr;i++)
                {
                  //System.out.println(k);
                  //if(k.y==l.y)
                  Point p1=new Point(i,k.y);
                  if(right.SMatr.containsKey(p1))
                  {
                    Point p2=new Point(k.x,i);
                    {
                      double buf;
                      if(result.containsKey(p2))
                      {
                        buf = result.get(p2) + SparseMatrix.this.SMatr.get(k) * right.SMatr.get(p1);
                        if (buf == 0) result.remove(p2);
                        else result.put(p2, buf);
                      } else {
                        buf = SparseMatrix.this.SMatr.get(k) * right.SMatr.get(p1);
                        result.put(p2, buf);
                      }
                    }
                  }
                }
              }

            }
            readyrow=increment();
          }
        }
      }

      private Scheduler(int numofthreads,SparseMatrix r) {
        readyrow0=0;
        atask = new Task[numofthreads];
        right=r;
        Rows=SparseMatrix.this.ArrayBuilder();
        result = new ConcurrentHashMap<>();
      }

      private synchronized int increment() {
        return readyrow0++;
      }

      private ConcurrentHashMap<Point,Double> control()
      {
        for(int i=0;i<atask.length;i++)
        {
          atask[i]=new Task();
        }
        try
        {
          for (Task task : atask) {
            task.thread.join();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        return result;
      }
    }




    if(nc==((SparseMatrix)o).nr&&SMatr!=null&&((SparseMatrix)o).SMatr!=null) {
      SparseMatrix tSMtx = ((SparseMatrix) o).transpose();
      int numofthreads = Runtime.getRuntime().availableProcessors();
      if (numofthreads > nr)
        numofthreads = nr;
      final Scheduler chief = new Scheduler(numofthreads,tSMtx);

      HashMap<Point,Double> result=new HashMap<>();
      ConcurrentHashMap<Point,Double> cresult=chief.control();

      for(Point p:cresult.keySet())
      {
        result.put(p,cresult.get(p));
      }

      return new SparseMatrix(result,nr,tSMtx.nr);
    }
    return null;
  }

  @Override
  public Matrix summ(Matrix o) {
    SparseMatrix SMtx = (SparseMatrix) o;
    if (nc == SMtx.nc && nr == SMtx.nr && SMatr != null && SMtx.SMatr != null) {
      HashMap<Point, Double> result = new HashMap<>();
      for (Point k : SMatr.keySet()) {
        if (SMtx.SMatr.containsKey(k)) {
          double buf;
          buf = SMatr.get(k) + SMtx.SMatr.get(k);
          result.put(k, buf);
        } else result.put(k, SMatr.get(k));
      }
      for (Point k : SMtx.SMatr.keySet()){
        if (!(SMatr.containsKey(k))){
          result.put(k, SMtx.SMatr.get(k));
        }
      }
      return new SparseMatrix(result, nc, nr);
    } else throw new RuntimeException("Размеры матриц не отвечают матричному уможению.");
  }

  private ConcurrentHashMap<Integer,Row> ArrayBuilder(){//Строит таблицу "номер строки"--"ключи к элементам этой строки"
    ConcurrentHashMap<Integer,Row> Array=new ConcurrentHashMap<>();
    int numofthreads = Runtime.getRuntime().availableProcessors();
    //int numofthreads=1;
    class Scheduler {//Распределитель задач
      int readyel0;
      Builder[] Builders;

      Scheduler(int numofthreads)
      {
        readyel0=0;
        Builders=new Builder[numofthreads];
      }

      void control()
      {
        for(int i=0;i<Builders.length;i++)
        {
          Builders[i]=new Builder();
        }
        try
        {
          for (Builder task : Builders) {
            task.thread.join();
          }
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }

      synchronized int increment() {
        return readyel0++;
      }

      class Builder implements Runnable {
        Thread thread;

        Builder() {
          thread = new Thread(this);
          thread.start();
          System.out.println(thread.getName() + " is working.");
        }

        Point[] Values = SMatr.keySet().toArray(new Point[0]);

        @Override
        public void run() {
          int readyel;
          readyel=increment();
          while(readyel<Values.length) {

            Point p=Values[readyel];
            synchronized (Array)
            {
              if (Array.containsKey(p.x)) {
                Array.get(p.x).position.add(p);
              } else {
                Row r = new Row();
                r.position.add(p);
                Array.put(p.x, r);
              }}
            readyel=increment();
          }
        }
      }
    }

    Scheduler chief=new Scheduler(numofthreads);
    chief.control();

    return Array;
  }



  private class Row//Класс строки,содержащей в себе ключи элементов одной строки
  {
    ArrayList<Point> position;

    Row()
    {
      position=new ArrayList<>();
    }
  }




  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
  @Override public boolean equals(Object o) {
    if(o instanceof DenseMatrix)
    {
      DenseMatrix DMtx=(DenseMatrix) o;
      if (SMatr == null || DMtx.DMatr == null) return false;
      if (nr == DMtx.nr && nc == DMtx.nc) {
        int nonzeros=0;
        for(int i=0;i<DMtx.nr;i++)
        {
          for(int j=0;j<DMtx.nc;j++)
          {
            if(DMtx.DMatr[i][j]!=0)
            {
              nonzeros++;
            }
          }
        }
        if(nonzeros!=SMatr.size()) return false;
        for (Point k: SMatr.keySet()) {
          if(DMtx.DMatr[k.x][k.y]==0)
            return false;
          if (DMtx.DMatr[k.x][k.y]!=SMatr.get(k)) {
            return false;
          }
        }
        return true;
      }
    }
    else if(o instanceof SparseMatrix)
    {
      SparseMatrix SMtx=(SparseMatrix)o;
      if (SMatr == null || SMtx.SMatr == null) return false;
      if (SMtx.SMatr == SMatr) return true;
      if (this.hashCode() != SMtx.hashCode()) return false;
      if (nr != SMtx.nr || nc != SMtx.nc) return false;
      if (SMatr.size()!=SMtx.SMatr.size())return false;
      for (Point p:SMatr.keySet()) {
        if (SMatr.get(p)-(SMtx.SMatr.get(p))!=0)
          return false;
      }return true;


    }
    return false;
  }

  @Override
  public int hashCode() {
    int hsh=Objects.hash(nr,nc);
    for(Point p:SMatr.keySet())
    {
      hsh+=(SMatr.get(p).hashCode()<<2)+31;
    }

    return hsh;
  }

  @Override public String toString() {
    if(SMatr==null) throw new RuntimeException("Встречена пустая матрица");
    StringBuilder resBuilder = new StringBuilder();
    resBuilder.append('\n');
    for (int i = 0; i < nr; i++) {
      resBuilder.append('[');
      for (int j = 0; j < nc; j++) {
        Point p=new Point(i,j);
        if (SMatr.containsKey(p)) {
          resBuilder.append(SMatr.get(p));
        } else {
          resBuilder.append(0.0);
        }
        if (j < nc - 1)
          resBuilder.append(" ");
      }
      resBuilder.append("]\n");
    }
    return resBuilder.toString();
  }
}