//Меню
//Отображение меню по кнопке
function showMenu() {
    const menuList = document.getElementById('menu_list');
    if (menuList.style.display === 'none') {
        menuList.style.display = 'block';
    } else {
        menuList.style.display = 'none';
    }
}

//Скрытие меню (при нажатии на основную часть экрана)
function hideMenu() {
    document.getElementById('menu_list').style.display = 'none';
    document.getElementById('diagram_name').blur();
    document.getElementById('diagram_code').blur();
}

//Активация меню
window.onload = function () {
    if(document.getElementById('menu_button') !== null) {
        document.getElementById('menu_button').click();
    }
}


//Проверка авторизации пользователя
function checkUserAuthorization(){
    let userName = document.getElementById('user-name');
    return userName.textContent !== '';
}

//Вывод сообщения
function messageOutput(type, messageText){
    let messageBox = document.getElementById('message_box');
    let message = messageBox.querySelector('.message');
    let newMessage = message.cloneNode(true);

    //Настройка сообщения
    switch (type){
        case 'error':
            newMessage.style.background = '#FF4444';
            break;
        case 'warning':
            newMessage.style.background = '#FFBB33';
            break;
    }
    newMessage.innerHTML = messageText;
    newMessage.style.display = 'block';

    //Обновление сообщения
    messageBox.removeChild(message);
    messageBox.appendChild(newMessage);
}

//Страница документации
//Поиск (по документации)
function searchText() {
    let searchText = document.getElementById('searchInput').value;
    let content = document.getElementById('content');
    let items = content.querySelectorAll('p, h1, h2');

    items.forEach(item => {
        let itemText = item.textContent;
        if (itemText.toLowerCase().includes(searchText.toLowerCase())) {
            let regex = new RegExp(searchText, 'ig');
            item.innerHTML = itemText.replace(regex, match => `<span style="background: yellow">${match}</span>`);
        } else {
            item.innerHTML = itemText;
        }
    });
}


//Страница со списком диаграмм
//Поиск (по списку)
function searchDiagram() {
    let searchText = document.getElementById('searchInput').value;
    location.href = '/diagram/list?searchText=' + searchText;
}

//Активация поиска при нажатии на Enter
function searchDiagramByKeyup(event) {
    if (event.keyCode === 13) {
        searchDiagram();
    }
}


//Страница работы с диаграммой
//Обработка кода диаграммы
function getFormattedCodeForDB(code) {
    let strings = code.split(/\n/g);
    for (let i = 0; i < strings.length; i++) {
        strings[i] = strings[i].trim();
        if (!strings[i].includes('{') && !strings[i].includes('}')) {
            strings[i] = '    ' + strings[i];
        }
    }
    return strings.join('\n');
}

function getFormattedCodeForDiagram(code) {
    let refs = getRefs(code)
    let result = 'erDiagram\n' + code
        .replace(/\/\/.*/g, '').replace(/pk/g, 'PK').replace(/\[/g, '')
        .replace(/not null/g, '"NN"').replace(/ ref:/g, ', ref:').replace(/ref:(.*?)[,\]]/g, 'FK').replace(/, /g, ' ')
        .replace(/]/g, '');

    return result + refs;
}

function getRefs(code) {
    let refs = '';
    let strings = code.split(/\n/g);
    for (let i = 0; i < strings.length; i++) {
        strings[i] = strings[i].trim();
        if (strings[i].includes('ref:')) {
            refs += strings[i].match(/ref:(.*?)[,\]]/g) + strings[i].split(/ +/g)[0];
        }
    }
    refs = refs.replace(/ref: /g, '\n').replace(/ < /g, ' ||--o{ ')
        .replace(/ > /g, ' }o--|| ').replace(/ - /g, ' ||--|| ')
        .replace(/[,\]]/g, ' : ')

    return refs
}