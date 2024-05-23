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

    const diagramName = document.getElementById('diagram_name');
    const diagramCode = document.getElementById('diagram_code');
    if(diagramName !==  null && diagramCode !== null){
        diagramName.blur();
        diagramCode.blur();
    }
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
        case 'info':
            newMessage.style.background = '#33b5e5';
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
    let searchText = document.getElementById('search_input').value;
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

//Поиск по списку при потере фокуса
function search() {
    const searchText = document.getElementById('search_input').value;
    location.href = location.href.split('?')[0] + '?searchText=' + searchText;
}
//Активация поиска при нажатии на Enter
function searchByKeyup(event) {
    if (event.keyCode === 13) {
        const searchText = document.getElementById('search_input');
        searchText.blur();
    }
}