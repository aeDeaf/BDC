# Установка и запуск программы управления контейнерами (BDC)

Для установки программы необходимо предназначен скрипт **install.sh** из папки **distributions**.

### Последовательность установки

1. Склонировать репозиторий
2. Запустить скрипт **install.sh** из папки **distributions** с правами суперпользователя: **sudo ./install.sh**
3. После завершения работы скрипта выполнить команду **sudo usermod -aG docker $USER**
4. Перезагрузить компьютер
5. Для запуска программы используется скрипт **start.sh**, а для остановки - **stop.sh**. Их нужно запускать без прав суперпользователя, то есть без **sudo**.
6. Для открытия пользовательского интерфейса необходимо в браузере перейти по адресу **http://localhost:3000**.

### Краткая инструкция по работе с программой

На основном окне программы, которое открывается при переходе по адресу **http://localhost:3000**,
расположены кнопки, выполняющие все основные функции. Большая синяя кнопка со знаком "+" позволяет добавить контейнер.
После добавления контейнера, он появляется в таблице, и в соответствующей строке начинаются отображаться кнопки
для запуска/остановки контейнера (кнопки старт/стоп) и его обновления (кнопка с 3 точками).
После нажатия кнопки запуска контейнера, через некоторое время откроется окно терминала, в котором уже будет настроено
подключение к контейнеру по SSH. Для начала работы требуется выполнить команду "**. init.sh**", которая выполнит
все необходимые настройки.

### Потенциальные проблемы и пути их решения

- Если не получается запустить sh скрипты на выполнение, то возможно, что у них сбился флаг x (executable),
  который позволяет запускать файлы как исполняемые. Для исправления этого можно воспользоваться командой
  **chmod +x ./install.sh** (аналогично для **start.sh** и **stop.sh**).
- В случае, если есть подозрение, что программа зависла, то всегда можно перезагрузить страницу в браузере, обычно
  это решает все проблемы.
- Изредка, только что созданный контейнер не запускается корректно (окно терминала не открывается).
  В такой ситуации самый простой выход - остановить контейнер и запустить его заново.
- Если программа не запустилась (при переходе по адресу **http://localhost:3000** ничего не появилось), то
  необходимо убедиться, что пользователь был добавлен в группу docker (**sudo usermod -aG docker $USER**).
  Если это не помогает, то можно перейти в папку **/var/lib/bdc** и выполнить команду
  **docker-compose up**. Если после выполнения этой команды появляется сообщение о том, что Docker сервис
  недоступен, то имеет смысл попробовать его перезапустить **sudo systemctl restart docker**.
- Если на момент установки у вас уже был установлен postgresql, то может возникнуть проблема при установке,
  проявляющаяся в том, что в процессе выполнения скрипта он начнет запрашивать пароль от пользователя postgres.
  В такой ситуации можно пойти одним из двух путей - либо ввести пароли 8 раз (для 8 команд), либо изменить способ
  авторизации c **md5** на **peer** в файле **pg_hba.conf**. Для поиска этого файла можно воспользоваться командой
  **cd / && sudo find . -name pg_hba.conf**. После внесения изменений в файл требуется перезагрузить
  сервис postgresql: **sudo systemctl restart postgresql**