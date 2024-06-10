//Редактор кода
import {createCodeMirror} from "./codemirror_script.js"
//Создание диаграммы
import {drawDiagram} from "./create_diagram_script.js"
//Экспорт диаграммы
import {exportDiagram, getFormattedCodeForDB} from "./export_diagram_script.js"


//Обработчики событий
//Отслеживание загрузки страницы
window.onload = async function () {
    //Активация меню, если кнопка есть на странице
    const menuButton = document.getElementById('menu_button');
    if (menuButton !== null) {
        menuButton.click();
    }
    //Активация истории, если кнопка есть на странице
    const historyButton = document.getElementById('history_button');
    if (historyButton !== null) {
        historyButton.click();
    }
    //Скрытие кнопки сохранения, если пользователь неакторизован или доступ к диаграмме ограничен
    if (menuButton == null || isAccessForRead()) {
        const saveButtonBox = document.getElementById('save_button_box');
        saveButtonBox.style.visibility = 'hidden';
    }

    let editor = createCodeMirror(isAccessForRead()); //Настройка текстового редактора
    window.myGlobalObject = {
        editorObj: editor
    };
    await changeTheme();
    resizeWorkZone();
    await drawDiagram();
    updateDiagram();
}

//Отслеживание закрытия страницы (закрытие вкладки, переход по ссылке, обновление страницы)
window.onbeforeunload = function (e) {
    //Проверка несохранения
    const saveButton = document.getElementById('save-button');
    if (saveButton.style.background === 'rgb(151, 2, 167)') {
        //Вывод сообщения о несохранении изменений
        const confirmationMessage = 'Вы уверены, что хотите покинуть страницу?'; //Сообщение (необходимо для некоторых браузеров)
        e.returnValue = confirmationMessage;
        return confirmationMessage;
    }
}

//Отслеживание нажатия клавиш Ctrl+S и сохранение кода описания диаграммы
document.onkeydown = function (e) {
    saveChangesByKey(e);
}


//Установка обработчиков обновления диаграммы
function updateDiagram() {
    //Отслеживание изменения размеров страницы
    window.addEventListener('resize', async function () {
        resizeWorkZone();
        await drawDiagram();
    });

    // Отслеживание изменения кода описания диаграммы
    const editor = window.myGlobalObject.editorObj;
    editor.on("change", async function () {
        await drawDiagram();
        changeSaveStatus();
    });
    // Отслеживание изменения темы
    const switchTheme = document.getElementById('switch_theme');
    switchTheme.onclick = async function () {
        await changeTheme();
        changeSaveStatus();
    }
    // Отслеживание изменения названия
    const diagramName = document.getElementById('diagram_name');
    diagramName.onchange = function () {
        changeSaveStatus();
    }
    diagramName.onkeyup = function () {
        //Активация сохранения при нажатии на Enter
        if (event.keyCode === 13) {
            diagramName.blur();
        }
    }
    // Отслеживание нажатие кнопки сохранить
    const saveButton = document.getElementById('save-button');
    saveButton.onclick = function () {
        saveChanges();
    }
    // Отслеживание изменения названия
    const exportSelector = document.getElementById('export_selector');
    exportSelector.onchange = function () {
        exportDiagram();
    }
}

//Изменение размеров редактора кода и диаграммы
function resizeWorkZone(){
    const header = document.querySelector('.header1');
    const sidebar = document.getElementById('sidebar');
    const content = document.getElementById('content');
    const headerHeight = header.offsetHeight;
    const windowHeight = window.innerHeight;

    sidebar.style.marginTop = headerHeight + 'px';
    sidebar.style.height = windowHeight - headerHeight + 'px';
    content.style.marginTop = headerHeight + 'px';
    content.style.height = windowHeight - headerHeight + 'px';
}

//Изменение статуса сохранения
function changeSaveStatus() {
    const saveStatusIcon = document.getElementById('save-status-icon');
    const saveButton = document.getElementById('save-button');
    saveStatusIcon.src = '/images/Save_required.png';
    saveButton.style.background = '#9702A7';
}

//Изменение темы страницы
async function changeTheme() {
    const checkbox = document.getElementById('checkbox_theme');
    const content = document.getElementById('content');
    const errorText = document.getElementById('error_text');

    //Изменение фона и сообщения об ошибке
    if (checkbox.checked) {
        content.style.backgroundColor = '#5A4C68';
        errorText.style.color = '#ffffff';
    } else {
        content.style.backgroundColor = '#ffffff';
        errorText.style.color = '#311F46';
    }
    await drawDiagram();
}

//Сохранение изменений
function saveChanges() {
    if (checkUserAuthorization()) {
        if (!isAccessForRead()) {
            const designTheme = getDesignThemeForSave();
            const diagramName = getNameForSave();
            const diagramCode = getCodeForSave();
            //Проверка несохранения
            const saveButton = document.getElementById('save-button');
            if (saveButton.style.background === 'rgb(151, 2, 167)') {

                window.onbeforeunload = null;
                location.href += '/save?designTheme=' + designTheme + '&diagramName=' + diagramName
                    + '&diagramCode=' + diagramCode;
            }
        } else {
            messageOutput('error', 'Невозможно сохранить изменения, доступ только на просмотр диаграммы')
        }
    } else {
        messageOutput('error', 'Невозможно сохранить изменения без авторизации')
    }
}

//Активация сохранения при нажатии клавиш Ctrl+S
function saveChangesByKey(event) {
    if (event.ctrlKey && event.keyCode === 83) { // 83 - код клавиши "S"
        saveChanges();
        event.preventDefault(); //Предотвращение стандартного действия браузера при нажатии Ctrl + S
    }
}

//Проверка уровня доступа диаграммы
function isAccessForRead() {
    const diagramNameInput = document.getElementById("diagram_name");
    return diagramNameInput.readOnly;
}

//Получение темы дизайна
function getDesignThemeForSave() {
    let checkbox = document.getElementById('checkbox_theme');
    let designTheme = "light";
    if (checkbox.checked) {
        designTheme = "dark";
    }
    return designTheme;
}

//Получение кода диаграммы и обработка запрещенных в url символов
function getNameForSave() {
    let diagramName = document.getElementById('diagram_name').value;
    //Замена запрещенных символов для URL
    diagramName = diagramName.replace(/\n/g, '').replace(/\\/g, '').replace(/\//g, '').replace(/\{/g, '')
        .replace(/}/g, '').replace(/\[/g, '').replace(/]/g, '').replace(/</g, '').replace(/>/g, '')
        .replace(/"/g, '').replace(/\|/g, '').replace(/\^/g, '').replace(/`/g, '');
    return diagramName;
}

//Получение кода диаграммы и обработка запрещенных в url символов
function getCodeForSave() {
    const editor = window.myGlobalObject.editorObj;
    let diagramCode = editor.doc.getValue();
    diagramCode = getFormattedCodeForDB(diagramCode)
    //Замена запрещенных символов для URL
    diagramCode = diagramCode.replace(/\n/g, '*n').replace(/\//g, '%2F').replace(/\{/g, '%7B').replace(/}/g, '%7D')
        .replace(/\[/g, '%5B').replace(/]/g, '%5D').replace(/</g, '%3C')
        .replace(/\\/g, '').replace(/>/g, '').replace(/"/g, '').replace(/\|/g, '').replace(/\^/g, '').replace(/`/g, '')
    return diagramCode;
}

