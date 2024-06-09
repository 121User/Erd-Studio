//Изменение названия группы при потере фокуса
function renameGroup() {
    const nameText = document.getElementById('name_input').value;
    const groupId = location.href.split('/')[4];
    location.href = '/group/' + groupId + '/rename?nameText=' + nameText;
}
//Активация изменения названия группы при нажатии на Enter
function renameGroupByKeyup(event) {
    if (event.keyCode === 13) {
        const nameText = document.getElementById('name_input');
        nameText.blur();
    }
}

//Изменение роли пользователя в группе
function changeRole() {
    const select = document.getElementById("role_selector");
    const selectedOption = select.options[select.selectedIndex];
    const selectedValue = selectedOption.value;
    const userId = selectedOption.dataset.role
    const groupId = location.href.split('/')[4];

    //Отправка запроса на сервер
    switch (selectedValue) {
        case "participant":
            location.href = '/group/' + groupId + '/participant/list/change-role/' + userId + '?role=1';
            break
        case "admin":
            location.href = '/group/' + groupId + '/participant/list/change-role/' + userId + '?role=2';
            break
    }
}

//Изменение роли пользователя в группе
function changeGroupAccess() {
    const select = document.getElementById("access_level_selector");
    const selectedOption = select.options[select.selectedIndex];
    const selectedValue = selectedOption.value;
    const groupId = location.href.split('/')[4];

    //Отправка запроса на сервер
    switch (selectedValue) {
        case "entry access":
            location.href = '/group/' + groupId + '/change-access/?level=1';
            break
        case "access is closed":
            location.href = '/group/' + groupId + '/change-access/?level=2';
            break
    }
}