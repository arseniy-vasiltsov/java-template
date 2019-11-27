package edu.spbu.matrix;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Разряженная матрица
 */
public class SparseMatrix implements Matrix
{ 
  public HashMap<Integer, HashMap<Integer, Double>> map;
  public int row;
  public int col;
  
  /**
   * загружает матрицу из файла
   * @param fileName
   */
  public SparseMatrix(String fileName) {
    col = 0;
    row = 0;
    HashMap<Integer, HashMap<Integer, Double>> result = new HashMap<Integer, HashMap<Integer, Double>>();
    try {
      File f = new File(fileName);
      Scanner input = new Scanner(f);
      String[] line = {};
      HashMap<Integer, Double> tmp;

      while (input.hasNextLine()) {
        tmp = new HashMap<Integer, Double>();
        line = input.nextLine().split(" ");
        for (int i=0; i<line.length; i++) {
          if (line[i]!="0") {
            tmp.put(i, Double.parseDouble(line[i]));  //записываем: ключ - номер, значение - число, преобразованное в double
          }
        }
        if (tmp.size()!=0) {
          result.put(row++, tmp);
        }
      }
      col = line.length;
      map = result;
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
 
  
  public SparseMatrix(HashMap<Integer, HashMap<Integer, Double>> map, int row, int col) {
    this.map = map;
    this.row = row;
    this.col = col;
  }
   
  @Override  public Matrix mul(Matrix o) { //однопоточное умножение матриц, выбираем нужное умножение
    if (o instanceof DenseMatrix) {
      return mul((DenseMatrix) o);
    }
    if (o instanceof SparseMatrix) {
      return mul((SparseMatrix) o);
    }
    else return null;
  }
  
  public SparseMatrix transpose () {  //транспонирование матриц
    HashMap<Integer, HashMap<Integer, Double>> result = new HashMap<Integer, HashMap<Integer, Double>>();
    for (HashMap.Entry<Integer, HashMap<Integer, Double>> row : map.entrySet()){
      for (HashMap.Entry<Integer, Double> elem : row.getValue().entrySet()) {
        if (!result.containsKey(elem.getKey())) {
          result.put(elem.getKey(), new HashMap<Integer, Double>());
        }
        result.get(elem.getKey()).put(row.getKey(), elem.getValue());
      }
    }
    return new SparseMatrix(result, col, row);
  }
  
  
  private DenseMatrix mul(DenseMatrix d) {
    double[][] dT = d.transpose();
    double[][] result = new double[row][dT.length];
    double sum = 0;
    for (Map.Entry<Integer, HashMap<Integer, Double>> row1 : map.entrySet()){
      for (int j = 0; j<dT.length; j++) {
        for (HashMap.Entry<Integer, Double> elem : row1.getValue().entrySet()) {
          if (row1.getValue().containsKey(elem.getKey())) {
            sum += elem.getValue()*dT[j][elem.getKey()];
          }
        }
        result[row1.getKey()][j] = sum;
        sum = 0;
      }
    }
    return new DenseMatrix(result);
  }
  
  private SparseMatrix mul(SparseMatrix s) { // умножение разряженной на разряженную
    SparseMatrix sT = s.transpose();
    HashMap<Integer, HashMap<Integer, Double>> result = new HashMap<Integer, HashMap<Integer, Double>>();
    double sum = 0;
    for (HashMap.Entry<Integer, HashMap<Integer, Double>> row1 : map.entrySet()){
      for (HashMap.Entry<Integer, HashMap<Integer, Double>> row2 : sT.map.entrySet()) {
        for (HashMap.Entry<Integer, Double> elem : row1.getValue().entrySet()) {
          if (row2.getValue().containsKey(elem.getKey())) {
            sum += elem.getValue()*row2.getValue().get(elem.getKey());
          }
        }
        if (sum != 0) {
          if (!result.containsKey(row1.getKey())) {
            result.put(row1.getKey(), new HashMap<Integer, Double>());
          }
          result.get(row1.getKey()).put(row2.getKey(), sum);
        }
        sum = 0;
      }
    }
    return new SparseMatrix(result, row, s.col);
  }

  
  
  /**
   * многопоточное умножение матриц
   *
   * @param o
   * @return
   */
  @Override public Matrix dmul(Matrix o)
  {
    return null;
  }

  /**
   * спавнивает с обоими вариантами
   * @param o
   * @return
   */
   @Override
  public boolean equals(Object o) { //сравнивение
    boolean y = true;
    if (o instanceof DenseMatrix) {
      DenseMatrix tmp = (DenseMatrix)o;
      if (tmp.data.length == row && tmp.data[0].length == col) {
        for (int i = 0; i<row; i++) {
          if (map.containsKey(i)) {
            for (int j = 0; j<col; j++) {
              if (map.get(i).containsKey(j)) {
                if (map.get(i).get(j) != tmp.data[i][j]) {
                  y = false;
                }
              } else {
                if (tmp.data[i][j] != 0) {
                  y = false;
                }
              }
            }
          } else {
            for (int j = 0; j < col; j++) {
              if (tmp.data[i][j] != 0) {
                y = false;
              }
            }
          }
        }
      } else {
        y = false;
      }
    } else if (o instanceof SparseMatrix) {
      SparseMatrix tmp = (SparseMatrix) o;
      if (tmp.col == col && tmp.row == row) {
        for (int i = 0; i<row; i++) {
          if (map.containsKey(i) && tmp.map.containsKey(i))  {
            for (int j = 0; j<col; j++) {
              if (map.get(i).containsKey(j) && tmp.map.get(i).containsKey(j)) {
                if (map.get(i).get(j).doubleValue() != tmp.map.get(i).get(j).doubleValue()) {
                  y = false;
                }
              } else if (map.get(i).containsKey(j) || tmp.map.get(i).containsKey(j)) {
                y = false;
              }
            }
          } else if (map.containsKey(i) || tmp.map.containsKey(i)) {
            y = false;
          }
        }
      } else {
        y = false;
      }
    }
    return y;
  }
}

