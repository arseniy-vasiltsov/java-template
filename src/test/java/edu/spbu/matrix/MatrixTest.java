package edu.spbu.matrix;

import edu.spbu.MatrixGenerator;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MatrixTest
{
  /**
   * ожидается 4 таких теста
   */
  //Тест умножения плотных матриц. Ожидается успех.
  @Test
  public void mulDDEx1() {
    Matrix m1 = new DenseMatrix("m1.txt");
    Matrix m2 = new DenseMatrix("m2.txt");
    Matrix actual=m1.mul(m2);
    Matrix expected = new DenseMatrix("result.txt");
    System.out.println("expected:"+(expected).toString());
    System.out.println("actual:"+((DenseMatrix)actual).toString());
    assertEquals(expected, actual);
  }

  @Test
  public void MultyMulDD() throws IOException {

    new MatrixGenerator(1,1,"Generated1.txt",1000).generate();
    new MatrixGenerator(1,1,"Generated2.txt",1000).generate();

    Matrix m1 = new DenseMatrix("Generated1.txt");
    Matrix m2 = new DenseMatrix("Generated2.txt");

    long start=System.currentTimeMillis();
    Matrix M1=m1.mul(m2);
    long finish=System.currentTimeMillis();
    System.out.println(finish-start);

    start = System.currentTimeMillis();
    Matrix M2=m1.dmul(m2);
    finish = System.currentTimeMillis();
    System.out.println(finish-start);



    assertEquals(M1, M2);
  }



  @Test
  public void muld() throws IOException {
    new MatrixGenerator(1,1,"Generated1.txt",1000).generate();
    new MatrixGenerator(1,1,"Generated2.txt",1000).generate();
  }

  //Тест умножения плотных матриц. Ожидается успех.
  @Test
  public void mulDDEx2() {
    Matrix m1 = new DenseMatrix("m3.txt");
    Matrix m2 = new DenseMatrix("m4.txt");
    Matrix actual=m1.mul(m2);
    Matrix expected = new DenseMatrix("result2.txt");
    System.out.println("expected:"+(expected).toString());
    System.out.println("actual:"+((DenseMatrix)actual).toString());
    assertEquals(expected, actual);
  }

  //Тест сложения плотных матриц. Ожидается успех.
  @Test
  public void tmsum() {
    Matrix m1 = new DenseMatrix("A1.txt");
    Matrix m2 = new DenseMatrix("A2.txt");
    Matrix actual= m1.summ(m2);
    System.out.println("expected:"+(actual).toString());
  }


  @Test
  public void MultyMulSS() throws IOException {
    new MatrixGenerator(1,4,"SparseGenerated1.txt",1000).generate();
    new MatrixGenerator(1,4,"SparseGenerated2.txt",1000).generate();

    Matrix m1 = new SparseMatrix("SparseGenerated1.txt");
    Matrix m2 = new SparseMatrix("SparseGenerated2.txt");

    long start=System.currentTimeMillis();
    Matrix M1=m1.mul(m2);
    long finish=System.currentTimeMillis();
    System.out.println(finish-start);

    start = System.currentTimeMillis();
    Matrix M2=m1.dmul(m2);
    finish = System.currentTimeMillis();
    System.out.println(finish-start);



    assertEquals(M1, M2);
  }


  //Тест умножения разреженных матриц. Ожидается успех.
  @Test
  public void mulSSEx2() {
    Matrix m1 = new SparseMatrix("A1.txt");
    Matrix m2 = new SparseMatrix("A2.txt");
    Matrix actual=m1.mul(m2);
    Matrix expected=new SparseMatrix("A1xA2.txt");
    System.out.println("expected:"+(expected).toString());
    System.out.println("actual:"+((SparseMatrix)actual).toString());
    assertEquals(expected, actual);
  }

  //Тест сложения разреженных матриц. Ожидается успех.
  @Test
  public void stsumm() {
    Matrix m1 = new SparseMatrix("A1.txt");
    Matrix m2 = new SparseMatrix("A2.txt");
    Matrix actual=m1.summ(m2);
    System.out.println("actual:"+((SparseMatrix)actual).toString());
  }

  //Тест умножения больших разреженных матриц.
  @Test
  public void mulSSGiant() {
    Matrix m1 = new SparseMatrix("SparseGiant1.txt");
    Matrix m2 = new SparseMatrix("SparseGiant2.txt");
    Matrix expected=new SparseMatrix("ResGiant.txt");
    assertEquals(expected, m1.dmul(m2));
  }

  //Тест умножения разреженной матрицы и плотной. Ожидается успех.
  @Test
  public void mulSDEx1() {
    Matrix m1 = new SparseMatrix("A1.txt");
    Matrix m2 = new DenseMatrix("A2.txt");
    Matrix actual=m1.mul(m2);
    Matrix expected=new DenseMatrix("A1xA2.txt");
    System.out.println("expected:"+(expected).toString());
    System.out.println("actual:"+((DenseMatrix)actual).toString());
    assertEquals(expected, actual);
  }

  //Тест умножения плотной матрицы на разреженную. Ождиается успех.
  @Test
  public void mulDSEx1() {
    Matrix m1 = new DenseMatrix("A1.txt");
    Matrix m2 = new SparseMatrix("A2.txt");
    Matrix actual=m1.mul(m2);
    Matrix expected=new DenseMatrix("A1xA2.txt");
    System.out.println("expected:"+(expected).toString());
    System.out.println("actual:"+((DenseMatrix)actual).toString());
    assertEquals(expected, m1.mul(m2));
  }

  //Тест на неподходящие размеры множителей. Ожидается успех.
  @Test(expected=RuntimeException.class)
  public void Failmul1() {
    Matrix m1 = new SparseMatrix("SparseA1.txt");
    Matrix m2 = new SparseMatrix("FailedTest1.txt");
    Matrix expected = new SparseMatrix("ResA1xA2.txt");
    assertEquals(expected, m1.mul(m2));
  }

  //Тест на наличие ненулевого элемента в неожидаемой позиции. Ожидается успех.
  @Test
  public void Failmul2() {
    Matrix m1 = new SparseMatrix("SparseA1.txt");
    Matrix m2 = new SparseMatrix("SparseA2.txt");
    Matrix actual=m1.mul(m2);
    Matrix expected = new SparseMatrix("FailedTest2.txt");
    System.out.println("expected:"+(expected).toString());
    System.out.println("actual:"+((SparseMatrix)actual).toString());
    assertNotEquals(expected, m1.mul(m2));
  }

  @Test(expected=NullPointerException.class)
  public void Failmul3() {
    Matrix m1 = new SparseMatrix(null,0,0);
    Matrix m2 = new SparseMatrix(null,0,0);
    Matrix actual=m1.mul(m2);
    System.out.println("actual:"+((SparseMatrix)actual).toString());
  }
}