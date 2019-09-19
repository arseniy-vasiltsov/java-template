package edu.spbu.sort;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by artemaliev on 07/09/15.
 */
public class IntSort {
  public static void sort (int array[]) {
    int low = 0; //начало массива
    int high = array.length - 1; //конец массива
    IntSort.qsort(array, low, high); //быстрая сортировка
  }

  /*
   * Created by arseniyvasiltsov on 12/09/19
   */
  public static void qsort (int array[], int low, int high) {
    //в случае пустого массива
    if (array.length == 0)
      return;

    //выход из алгоритма
    if (low >= high)
      return;

    //находим опорный элемент
    int middle = low + (high - low) / 2;
    int opora = array[middle];

    //ищем элементы, которые нужно переставить
    int i = low, j = high;
    while (i <= j) {
      while (array[i] < opora) {
        i++;
      }

      while (array[j] > opora) {
        j--;
      }

      //переставляем элементы
      if (i <= j) {
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        i++;
        j--;
      }
    }

    //сортируем числа меньше опоры
    if (low < j)
      qsort(array, low, j);

    //сортируем числа больше опоры
    if (high > i)
      qsort(array, i, high);
  }

  public static void sort (List<Integer> list) {
    Collections.sort(list);
  }
}
