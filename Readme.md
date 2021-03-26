#### Запуск

Серверная часть - `Server/src/main/java/Server/Server.java`
Порт для клиентских подключений 1234, можно передать первым аргументом
Для для телнета 2345, можно задать вторым аргументов
JDBC jdbc:oracle:thin:@localhost:1521:xe, можно передать третьим аргументом

Пример запуска серверной части
`java.exe -jar Server-1.0-SNAPSHOT-jar-with-dependencies.jar 1234 2345 jdbc:oracle:thin:@localhost:1521:xe`

**Важно - выбарть свой сид для oracle**

Клиентская часть - `Client/src/main/java/Client/Client.java` - по умолчанию коннект localhost 1234

Пример запуска клиентской части
`java.exe --module-path "E:\Distr\Java\javafx-sdk-15.0.1\lib" --add-modules javafx.controls,javafx.fxml -jar Client-1.0-SNAPSHOT-jar-with-dependencies.jar`

**Важно - выбарть свой путь до javafx-sdk**

#### Основной функционал проекта

- [x] ААА - обязательно аутентификация и авторизация
- [x] Смена пароля, удаление аккаунта
- [x] Загрузка, скачивание файлов
- [x] 1 репозиторий - 1 юзер
- [x] Копирование, перемещение, удаление, сортировка файлов.
- [x] Создание папок - через контекстное меню в серверном (правом) окне
- [ ] Поиск файлов
- [x] Пометка на удаление / корзина - Просто удаление
- [X] Ограничение на размер

#### Homework 1 task

**Дописать код из тестового проекта, написав код, помеченный командами TODO. Подробнее - реализовать функционал:**

- [x] скачивания файла с сервера		можно выбирать в окне
- [x] загрузка файлов на сервер			можно выбирать в окне
- [x] удаления файла с сервера			можно выбирать в окне
- [x] вывода списка файлов сервера      вызывается при коннекте, удалении файла, загрузки файла на сервере

#### HomeWork 2 task

**Добавить к нашему NIO Telnet Server консольные команды:**

- [x] -help вывод помощи
- [x] ls вывод списка файлов
- [x] touch (имя файла) - создание файла
- [x] mkdir (имя директории) - создание директории
- [x] cd (path) - перемещение по дереву папок
- [x] rm (имя файла или папки) - удаление объекта
- [x] copy (src, target) - копирование файла
- [x] cat (имя файла) - вывод в консоль содержимого

**MyTODO:**

- [ ] Реализовать загрузку файлов в отдельном поток с прогресс баром
- [x] Реализовать разных пользователей (папки)
- [x] Придумать как отличать в интерфейсе на клиенте файлы и паки - локальной папки
- [x] Придумать как отличать в интерфейсе на клиенте файлы и паки - серверной папки
- [x] Подумать и реализовать интерфейс - не писать название файла для закачки на сервер
- [x] Доточить передачу списка файлов (окончание списка)
- [x] Реализовать хождение по папкам в клиенте
- [x] ls выровнять вывод чтобы не влиял размер наименования файла
- [x] Переделать File на Path (IO -> NIO)
- [x] Переделать Socket'ы на SockenChannel'ы (IO -> NIO)
