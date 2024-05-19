//Редактор кода
import {createCodeMirror} from "/js/codemirror_script.js"
//Создание диаграммы
import {drawDiagram} from "/js/create_diagram_script.js"

// //Для отладки без запуска приложения
// //Редактор кода
// import { createCodeMirror } from "/static/js/codemirror_script.js"
// //Создание диаграммы
// import { drawDiagram } from "/static/js/create_diagram_script.js"


//Обработчики событий
//Отслеживание загрузки страницы
window.onload = async function () {
    document.getElementById('menu_button').click();
    let editor = createCodeMirror(); //Настройка текстового редактора
    window.myGlobalObject = {
        editorObj: editor
    };
    await changeTheme(); //Настройка темы страницы
    await drawDiagram();
    updateDiagram(); //Установка обработчиков обновления диаграммы
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


//Обновление диаграммы
function updateDiagram() {
    window.addEventListener('resize', drawDiagram); //Отслеживание изменения размеров страницы
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
    // Отслеживание нажатие кнопкки сохранить
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


//Изменение статуса сохранения
function changeSaveStatus(){
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
        const designTheme = getDesignThemeForSave();
        const diagramName = getNameForSave();
        const diagramCode = getCodeForSave();
        //Проверка несохранения
        const saveButton = document.getElementById('save-button');
        if (saveButton.style.background === 'rgb(151, 2, 167)') {

            window.onbeforeunload = null;
            location.href += '/save?designTheme='
                + designTheme + '&diagramName=' + diagramName + '&diagramCode=' + diagramCode;
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
        .replace(/\[/g, '%5B').replace(/]/g, '%5D').replace(/\\/g, '').replace(/</g, '').replace(/>/g, '')
        .replace(/"/g, '').replace(/\|/g, '').replace(/\^/g, '').replace(/`/g, '')
    return diagramCode;
}

//Обработка кода диаграммы
function getFormattedCodeForDB(code) {
    let strings = code.split(/\n/g);
    for (let i = 0; i < strings.length; i++) {
        strings[i] = strings[i].trim();
        if (!strings[i].includes('{') && !strings[i].includes('}')) {
            strings[i] = '  ' + strings[i];
        }
    }
    return strings.join('\n');
}


//Экспорт
function exportDiagram() {
    const select = document.getElementById("export_selector");
    const selectedOption = select.options[select.selectedIndex].value;

    //Выбор функции экспорта
    switch (selectedOption) {
        case "pdf":
            exportPdf();
            break
        case "png":
            exportPng();
            break
        case "svg":
            exportSvg()
            break
        case "mssql":
            exportSql("mssql")
            break
        case "postgresql":
            exportSql("postgresql")
            break
    }
    select.selectedIndex = 0;
}

function exportSvg() {
    const diagramName = document.getElementById('diagram_name');
    const diagram = document.getElementById('diagram');
    const svgElement = diagram.querySelector('svg');

    //Удаление элементов управления масштабом
    const svgPanZoomControls = document.getElementById('svg-pan-zoom-controls');
    svgElement.removeChild(svgPanZoomControls);

    const svgContent = svgElement.outerHTML; //Получение кода SVG элемента
    const svgBlob = new Blob([svgContent], {type: 'image/svg+xml'});
    const svgUrl = URL.createObjectURL(svgBlob);//Создание URL-адреса

    svgElement.appendChild(svgPanZoomControls); //Возврат элементов управления масштабом
    window.saveAs(svgUrl, diagramName.value + '.svg'); //Отправка файла
}

function exportPng() {
    const diagram = document.getElementById('diagram');
    const svgElement = diagram.querySelector('svg');

    //Удаление элементов управления масштабом
    const svgPanZoomControls = document.getElementById('svg-pan-zoom-controls');
    svgElement.removeChild(svgPanZoomControls);

    domtoimage.toPng(svgElement, {})
        .then(function (dataUrl) {
            const diagramName = document.getElementById('diagram_name');
            window.saveAs(dataUrl, diagramName.value + '.png'); //Отправка файла
        })
        .catch(function (error) {
            console.error('Произошла ошибка:', error);
        })
        .finally(function () {
            svgElement.appendChild(svgPanZoomControls); //Возврат элементов управления масштабом
        });
}

function exportPdf() {
    const diagram = document.getElementById('diagram');
    const svgElement = diagram.querySelector('svg');

    //Удаление элементов управления масштабом
    const svgPanZoomControls = document.getElementById('svg-pan-zoom-controls');
    svgElement.removeChild(svgPanZoomControls);

    domtoimage.toPng(svgElement, {})
        .then(function (dataUrl) {
            const diagramName = document.getElementById('diagram_name');
            //Создание документа PDF
            const pdf = new jspdf.jsPDF();
            const pageSize = pdf.internal.pageSize; // Получение размера страницы
            //Размеры и положение изображения на листе
            pageSize.setWidth(diagram.offsetWidth);
            pageSize.setHeight(diagram.offsetHeight);

            pdf.addImage(dataUrl, 'PNG', 0, 0, diagram.offsetWidth, diagram.offsetHeight);
            pdf.save(diagramName.value + '.pdf'); //Отправка файла
        })
        .catch(function (error) {
            console.error('Произошла ошибка:', error);
        })
        .finally(function () {
            svgElement.appendChild(svgPanZoomControls); //Возврат элементов управления масштабом
        });
}

function exportSql(type) {
    location.href += '/export?type=' + type;
}