# lang
да, мой язык так называется

# Мотивация
1. Нужно писать курсач
2. Почему бы не сделать язык такой, какой я хочу

# Цель
1. Простой, удобный и эффективный

# Что хочется
1. Статическая строгая типизация
2. Стека нет - все объекты создаются в памяти
3. Компилируемый
4. Память - garbage collector
5. Указатели - в топку
6. Функции - объект
7. Всё объект
8. Регистры используются для вычисления значений выражений
9. Модель памяти - никакая - на барьерах
10. Синтаксис - минимум сахара

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

## Константы
string - всегда константа(нельзя менять массив)
если переменная нигде не переопределяется - константа

## volatile
выключить компиляторные оптимизации над переменной

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
### switch
switch эквивалентен if-else конструкции
```
switch
    case <expression> -> function
    case <expression> -> function
    ...
    default -> function
```
или 
```
switch expression
    case <expression> -> function
    case <expression> -> function
    ...
    default -> function
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

# Memory model
```
load_memory_barrier
store_memory_barrier 
```      
    
пример реализации атомика like Java:

Чтение:
```
load_memory_barrier
v = atomic
```

Запись:
```
atomic = v
store_memory_barrier
```
    
## Объявления

```
<name> <type>
```

пример

```
i8 v 
 или 
byte v
```

## Присваивания

```
<type> <name> = expression
```

## For

```

for <t> in <iterable>
    ...

```

## Interface

```
interface i
    method(a int, b i64) short
```

реализация интерфейса

```
interface i
    method(a int, b i64) short

class a : i
    (param type, ...) - конструктор
    method(a,b) -> a + b
```

## Наследование

```
class a : b
    method(a int, b i64) short

```

## Обобщенные типы

```
class a<t>
    method(a int, b i64) t

```

## Статические функции

в терминах джавы их не существует - есть просто функции(не методы)

## Обработка ошибок

специальный класс языка

```
class opt<t>
    variableValue t
    error e
    ok() bool
```

func(string a, string b) opt<int>

# Стандартная библиотека


```
class atomic<t>
    read() t
    write(t) 
```

```
class thread
    (() int)
    run()
    interrupted() bool
    stop() int
    join() int
    
```

```
class fiber
    (() int)
    run()
    
    resume()
    suspend()    

    interrupted() bool
    stop() int
    join() int

```

```
class socket
    read() int
    write(int)
    
```

```
class stdin
    read(...)
```

```
class stdout
    print(...)
```


## Пример программы

```

class a
    i8 i
    i64 t
    
    method(i8 a, i8 b) i16
        a + b

func(int n) i64
   n == 0 ? 0 : n==1 ? 1 : func(n-2) + func(n-1)

main(int a, string[] b) i8
    string s = "xyz"
    i8 i = 0
    while i < 40
        i64 n = func(i)
        i = i + 1

    a obj
    obj.method(1, 3)

    if obj.i == 8
        obj.i = 10
    elif obj.i == 9
        obj.i = 11
    else
        obj.i = 12

    switch t
        case 1 -> 
        case 2 ->
        default ->

    switch
        case t == 1 ->
        case t == 2 ->
        default -> 

    t thread(() -> 0)
    t.run()

    t.join()
    t.stop()

    return 0

```



