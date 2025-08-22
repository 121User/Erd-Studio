## **Мобильное приложение для чтения электронных документов**

## 1 UI/UX дизайн проекта

Дизайн приложения разработан в программе Figma.

При анализе и проектировании программного продукта определены следующие
основные экраны:

-   приветствия;

-   меню приложения;

-   всех документов;

-   коллекций;

-   списка документов определенной коллекции;

-   чтения документов.

Для данного приложения выбран графический логотип и помещен на фоне,
соответствующем теме приложения. На рисунке 1 представлен логотип
приложения.

<img width="230" height="241" alt="image" src="https://github.com/user-attachments/assets/61f7a162-c440-4488-8cd8-0edc2d8d67be" />

Рисунок 1 - Логотип приложения «ReaDocs»

На экране приветствия пользователю представлен логотип приложения. Экран
приветствия представлен на рисунке 2.

<img width="173" height="292" alt="image" src="https://github.com/user-attachments/assets/78d980c1-f457-4d6f-8d37-a05c438821eb" />

Рисунок 2 - Экран приветствия

Навигация между экранами осуществляется при помощи кнопок меню. Из меню
можно перейти на все основные экраны. Меню приложения представлено на
рисунке 3.

<img width="268" height="451" alt="image" src="https://github.com/user-attachments/assets/e9ec9fcd-2875-4014-8488-bb8c493ed3b0" />

Рисунок 3 - Меню приложения

Все документы на устройстве пользователь может посмотреть на экране «Все
документы». На экране со списком документов у пользователя есть
возможность добавить документ в коллекции, перейти на страницу чтения
документа, переименовать или удалить его. Экран со всеми документами
представлен на рисунке 4.

<img width="270" height="455" alt="image" src="https://github.com/user-attachments/assets/373d6a15-e651-471c-ac38-fb4c7d3d48fb" />

Рисунок 4 - Экран со всеми документами

На экране с коллекциями пользователь может создать коллекцию,
просмотреть список созданных коллекций, переименовать или удалить
коллекцию и перейти на экран просмотра списка документов в нужной
коллекции. Экран с коллекциями представлен на рисунке 5.

<img width="275" height="459" alt="image" src="https://github.com/user-attachments/assets/ce7d534a-a6cb-45c3-b22d-c50500a16a91" />

Рисунок 5 - Экран с коллекциями

После добавления в коллекцию по умолчанию документ в списке помечается
знаком коллекции. Так, например, документы, которые пользователь пометил
знаком «Избранное» расположены на соответствующем экране. На рисунке 6
показан экран со списком документов коллекции «Избранное».

<img width="273" height="461" alt="image" src="https://github.com/user-attachments/assets/92c7f371-5ee3-4ce5-9d32-a75ac86f50a8" />

Рисунок 6 - Экран со списком документов коллекции «Избранное»

При нажатии на документ открывается экран чтения документа. На нем
пользователь может изменять размер текста и перемещаться по документу.
На рисунке 7 показан экран для чтения документов.

<img width="547" height="440" alt="image" src="https://github.com/user-attachments/assets/a9726c38-ef01-43d5-9061-aa3462d5147f" />

Рисунок 7 - Экран для чтения документов

## 2 Разработка базы данных

В приложении возникает необходимость создания базы данных, так как нужно
хранить информацию о созданных пользователем коллекциях и вложенных в
эти коллекции документах. Для этой цели было решено использовать SQLite,
поскольку он позволяет хранить базу данных локально на устройстве
пользователя.

База данных состоит из трех таблиц: «Документы», «Коллекции» и таблицы,
связывающей их между собой. В таблице «Документы» необходимо хранить его
название, путь, формат и размер. В таблице «Коллекции» необходимо
хранить только ее название. Между таблицами «Documents» и «Collections»
существует связь многие-ко-многим, так как один документ может
находиться в нескольких коллекциях одновременно, а одна коллекция может
содержать несколько документов. Для реализации этой связи была создана
таблица «CollectionDocument».

На рисунке 8 представлена ER-диаграмма базы данных приложения.

<img width="837" height="315" alt="image" src="https://github.com/user-attachments/assets/c6d2782d-7a20-4d9b-bb57-2bfd867274e6" />

Рисунок 8 - ER-диаграмма базы данных

## 3 Описание разработанных процедур и функций

В приложении разработаны следующие функции:

-   вывод списка документов текущей коллекции;

-   добавление документа в коллекцию;

-   переименование и удаление документа;

-   чтение документа;

-   вывод списка коллекций;

-   создание коллекции;

-   переименование и удаление коллекции.

Ниже на листинге 1 представлен метод, который отвечает за формирование
списка документов и его вывод. Этот метод устанавливает адаптер с
корректным внешним видом для списка документов, вызывает метод
updateQuantityDocuments() для обновления количества документов в списке,
устанавливает переходы на страницы чтения PdfActivity и TxtActivity при
простом касании и открытие диалогового окна EditDocumentDialog при
долгом касании. В списке нет документов, то метод выводит информацию об
этом.

Листинг 1 - Метод для вывода списка документов текущей коллекции

~~~java
public void formationViews() {
    lvDocuments.setAdapter(adapter); //Установка адаптера в ListView
    updateQuantityDocuments(); //Обновление вывода количества документов
    //Открытие активности для просмотра документа при выборе документа из списка
    lvDocuments.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent;
            //Открытие активности для PDF-файла
            if (listDocuments.get(i).getDocFormat().equals("PDF")) {
                intent = new Intent(view.getContext(), PdfActivity.class);
            }
            //Открытие активности для TXT, DOC или DOCX файла
            else{
                intent = new Intent(view.getContext(), TxtActivity.class);
            }
            intent.putExtra("fileName", listDocuments.get(i).getDocName());
            intent.putExtra("filePath", listDocuments.get(i).getDocPath());
            startActivity(intent);
        }
    });
    lvDocuments.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
            Intent intent = new Intent(view.getContext(), EditDocumentDialog.class);
            intent.putExtra("fileName", listDocuments.get(i).getDocName());
            intent.putExtra("filePath", listDocuments.get(i).getDocPathWithFolderNumber());
            intent.putExtra("fileCollections", listDocuments.get(i).getDocCollections());
            startActivity(intent);
            return true;
        }
    });
    //Вывод информации об отсутствии документов в коллекции
    if(lvDocuments.getCount() == 0){
        lvDocuments.setVisibility(View.GONE);
        tvNoDocuments.setVisibility(View.VISIBLE);
    } else {
        lvDocuments.setVisibility(View.VISIBLE);
        tvNoDocuments.setVisibility(View.GONE);
    }
}
~~~


Ниже на листинге 2 представлен метод, который реализует добавление
документа в коллекцию. Этот метод получает имя коллекции и путь к
документу и определяет индексы коллекции и документа с помощью функций
getIdDocumentByPath() и getIdCollectionByName(). После метод создает
связь и записывает полученные индексы в таблицу CollectionDocument базы
данных.

Листинг 2 - Метод для добавления документа в коллекцию

~~~java
public void addDocumentInCollection(String nameColl, String pathDoc) {
    int idDoc = getIdDocumentByPath(pathDoc); //Индекс документа в таблице Documents
    int idColl = getIdCollectionByName(nameColl); //Индекс коллекции в таблице Collections
    SQLiteDatabase database = getBaseContext().openOrCreateDatabase("ReaDocs.db", MODE_PRIVATE, null);
    database.execSQL("INSERT INTO CollectionDocument VALUES (" + idColl + ", " + idDoc + ")");
    database.close();
}
~~~

Ниже на листинге 3 представлен метод, который используемый для
переименования документов. Этот метод вызывает метод проверки
уникальности нового пути документа. Если путь уникален, имя и путь
документа меняется на устройстве и в базе данных.

Листинг 3 - Метод для переименования документа

~~~java
public void renameDocument(String newNameDoc, String newPathDoc, String oldPathDoc) {
    //Получение файла по старому пути документа
    String[] oldPath = oldPathDoc.split(" --- ");
    String folderNumber = oldPath[0], oldPathDocWithoutFolderNumber = oldPath[1];
    File oldFile = new File(oldPathDocWithoutFolderNumber);
    //Получение файла по новому пути документа
    String[] newPath = newPathDoc.split(" --- ");
    String newPathDocWithoutFolderNumber = newPath[1];
    File newFile = new File(newPathDocWithoutFolderNumber);
    //Проверка уникальности пути документа
    if (checkPathDoc(newPathDoc)) {
        if (oldFile.renameTo(newFile)) {
            renameDocumentInDatabase(newNameDoc, newPathDoc, oldPathDocWithoutFolderNumber, folderNumber);
            //Уведомление об успешном изменении имени
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Имя файла успешно изменено на " + newNameDoc, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            //Уведомление об ошибке при изменении имени
            Toast toast = Toast.makeText(getApplicationContext(),
                    "Не удалось изменить имя файла", Toast.LENGTH_SHORT);
            toast.show();
        }
    } else {
        //Уведомление о неуникальности имени
        Toast toast = Toast.makeText(getApplicationContext(),
                "Документ с таким именем уже существует", Toast.LENGTH_SHORT);
        toast.show();
    }
}
~~~

Метод для удаления документа удаляет документ с устройства. Если
документ удален успешно, то документ удаляется из базы данных и из всех
коллекций в базе.

Классы для чтения документов позволяют прочитать текст документа,
изменить размер, перемещаться по тексту, вернуться в начальный размер по
умолчанию при долгом нажатии по области текста.

Ниже на листинге 4 представлен метод для создания коллекций. Этот метод
проверяет имя новой коллекции. Если имя уникально, то получается индекс
для коллекции и коллекция добавляется в таблицу Collections базы данных.

Листинг 4 - Метод для создания коллекции

~~~java
public void addCollection(String nameColl) {
    if (nameColl.equals("")) {
        Toast toast = Toast.makeText(getApplicationContext(),
                "Имя коллекции не может быть пустым", Toast.LENGTH_SHORT);
        toast.show();
    }
    else if (checkNameColl(nameColl)) {
        SQLiteDatabase database = getBaseContext().openOrCreateDatabase("ReaDocs.db", MODE_PRIVATE, null);
        Cursor queryColl = database.rawQuery("SELECT * FROM Collections;", null);
        int idColl = getIdCollectionToCreate();
        database.execSQL("INSERT INTO Collections VALUES " +
                "(" + idColl + ", '" + nameColl + "');");
        queryColl.close();
        database.close();
        //Уведомление об успешном создании файла
        Toast toast = Toast.makeText(getApplicationContext(),
"Коллекция \"" + nameColl + "\" успешно создана", Toast.LENGTH_SHORT);
        toast.show();
    } else {
        //Уведомление о неуникальности имени
        Toast toast = Toast.makeText(getApplicationContext(),
"Коллекция с таким именем уже существует", Toast.LENGTH_SHORT);
        toast.show();
    }
}
~~~

Рассмотренные листинги предоставляют ключевые функциональные возможности
мобильного приложения для чтения и группировки документов. Комбинация
этих функций позволяет создать интуитивно понятное и мощное приложение,
которое удовлетворяет потребностям пользователей в чтении электронных
книг.
