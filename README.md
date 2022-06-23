# lang


# Запуск

Выполняем скрипт ./setup.sh -s <директория для установки компилятора>
В этой директории выполняем скрипт ./run.sh -i <путь к проекту> -o <путь к исполняемому файлу>


# Цель
1. Простой, удобный и эффективный

# Что хочется

1. Статическая строгая типизация
2. Компилируемый
3. Память - garbage collector
4. Указатели - в топку
5. Функции - объект
6. Всё объект
7. Синтаксис - минимум сахара
8. Пакеты

# Типы

## Стандартные типы
i8, i16, i32, i64 - по битам
f32, f64

аналоги

уровень битности по умолчанию 8 - если у машины меньше или больше
то 8 * n

null

1. i8  - byte - минимум 8 бит - bool
2. i16 - short
3. i32 - int
4. i64 - long
5. f32 - float
7. f64 - double

## Массивы
i8[n], i64[n]

## Строки
string = i8[n]

Отдельный класс стандартной библиотеки

# Управляющие конструкции

Уровень вложенности - с помощью табуляции

### if, elif, else
```
if <expression>
    ...
elif <expression>
    ...
else
    ...
```

### while
```
while expression
    ...
```

### return
```
return
 или
return v
```

## Пакетность
Она есть, как в джаве


## Объявления

```
<name> <type>
```

## Объявление и определение

```
<type> <name> = expression
```

## Статические функции

в терминах джавы их не существует - есть просто функции(не методы)

# Функции

Сигнатура + название + стрелочка

```
(int[] arr, int length) void reverseArray ->
    int index = 0

    while index < length/2
        int c = arr[index]
        arr[index] = arr[length - index - 1]
        arr[length - index - 1] = c

        index++
```

# Классы и вызов методов

```
class Point
    int x
    int y

    (int x, int y) ->
        this.x = x
        this.y = y

    () int getX ->
        return this.x

    (int x) void setX ->
        this.x = x
```

# Многомерные массивы

```
() int main ->
    int i1 = 0
    int i2 = 0

    int size1 = 4
    int size2 = 4

    Point[][] array = new Point[size1][size2]

    while i1 < size1
        i2 = 0

        while i2 < size2
            array[i1][i2] = new Point(i1, i2)

            printNumber(array[i1][i2].x)
            putchar(32)
            printNumber(array[i1][i2].y)
            putchar(10)

            i2++

        i1++

    return scalar(array[2][2], array[size1-1][size2-1])
```


## Пример программы

Другие примеры можно поискать в папках project*

```

(int c) void putchar

class String
    int[] bytes

(int[] arr, int length) void reverseArray ->
    int index = 0

    while index < length/2
        int c = arr[index]
        arr[index] = arr[length - index - 1]
        arr[length - index - 1] = c

        index++

(int num) void printNumber ->
    int[] buffer = new int[16]

    int i = 0
    int j = 0

    if num == 0
        putchar(48)
        return


    int cur = num

    while cur != 0
        buffer[i] = cur % 10 + 48
        cur = cur / 10
        i++

    reverseArray(buffer, i)

    while j < i
        putchar(buffer[j])
        j++


(Point p1, Point p2) int scalar ->
    return p1.x * p2.x + p1.y * p2.y

class Point
    int x
    int y

    (int x, int y) ->
        this.x = x
        this.y = y

    () int getX ->
        return this.x

    (int x) void setX ->
        this.x = x

() int main ->
    int i1 = 0
    int i2 = 0

    int size1 = 4
    int size2 = 4

    Point[][] array = new Point[size1][size2]

    while i1 < size1
        i2 = 0

        while i2 < size2
            array[i1][i2] = new Point(i1, i2)

            printNumber(array[i1][i2].x)
            putchar(32)
            printNumber(array[i1][i2].y)
            putchar(10)

            i2++

        i1++

    return scalar(array[2][2], array[size1-1][size2-1])
```
