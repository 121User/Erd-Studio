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
}

//Активация меню
window.onload = function () {
    document.getElementById('menu_button').click();
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
//Сохранение темы страницы
function saveTheme() {
    changeTheme();
    const checkbox = document.getElementById('checkbox_theme');
    let designTheme = "light";
    if (checkbox.checked) {
        designTheme = "dark";
    }
    location.href += '/save?designTheme=' + designTheme;
}

//Изменение темы страницы
function changeTheme() {
    const checkbox = document.getElementById('checkbox_theme');
    const content = document.getElementById('content');

    if (checkbox.checked) {
        content.style.backgroundColor = '#5A4C68';
        let items = content.querySelectorAll('p, a');
        items.forEach(item => {
            item.innerHTML = item.textContent.replace(new RegExp(item.textContent, 'ig'),
                match => `<span style="color: white">${match}</span>`);
        });
    } else {
        content.style.backgroundColor = 'white';
        let items = content.querySelectorAll('p, a');
        items.forEach(item => {
            item.innerHTML = item.textContent.replace(new RegExp(item.textContent, 'ig'),
                match => `<span style="color: #311F46">${match}</span>`);
        });
    }
}

//Сохранение названия диаграммы (используется th)
function saveName() {
    let diagramName = document.getElementById('diagram_name').value;
    location.href += '/save?diagramName=' + diagramName;
}

//Сохранение кода диаграммы (используется th)
function saveCode() {
    let diagramCode = document.getElementById('diagram_code').value;
    diagramCode = getFormattedCodeForDB(diagramCode)
    diagramCode = diagramCode.replace(/\n/g, '*n').replace(/\\/g, "").replace(/\//g, "").replace(/\{/g, "*'")
        .replace(/}/g, "'*").replace(/\[/g, "*,").replace(/]/g, ",*");

    location.href += '/save?diagramCode=' + diagramCode;
}

//Активация сохранения при нажатии на Enter
function saveCodeByKeyup(event) {
    if (event.keyCode === 13) {
        saveCode();
    }
}

//Изменение размеров диаграммы при движении колеса мыши
function resizeDiagramByMouseWheel(event) {
    const diagram = document.getElementById('diagram');

    //Поддержка разных браузеров для определения направления прокрутки
    var delta = 0;
    if (event.deltaY) {
        delta = event.deltaY;
    } else if (event.wheelDelta) {
        delta = event.wheelDelta;
    } else if (event.detail) {
        delta = -event.detail;
    }

    if (delta < 0) {
        diagram.style.width = parseInt(diagram.style.width) + 10 + 'px';
        diagram.style.height = parseInt(diagram.style.height) + 10 + 'px';
    } else if (delta > 0){
        diagram.style.width = parseInt(diagram.style.width) - 10 + 'px';
        diagram.style.height = parseInt(diagram.style.height) - 10 + 'px';
    }
}

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
    let result = 'erDiagram\n' + code.replace(/pk/g, 'PK').replace(/\[/g, '').replace(/not null/g, '"NN"')
        .replace(/ ref:/g, ', ref:').replace(/ref:(.*?)[,\]]/g, 'FK').replace(/, /g, ' ').replace(/]/g, '');

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

function updateWindow() {
    changeTheme();
}


//Экспорт
function exportDiagram() {
    const select = document.getElementById("export");
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
    const svgContent = diagram.outerHTML; //Получение кода SVG элемента
    const svgBlob = new Blob([svgContent], {type: 'image/svg+xml'});
    const svgURL = URL.createObjectURL(svgBlob);//Создание URL-адреса

    const link = document.createElement('a'); //Ссылка для отправки файла
    link.href = svgURL;
    link.download = diagramName.value + '.svg'; //Отправка файла
    link.click();
    URL.revokeObjectURL(svgURL); //Очистка созданного URL
}

function exportPng() {
    const preElement = document.querySelector('.mermaid')
    //Создание временной копии и удаление лишних границ
    const clonedElement = preElement.cloneNode(true);
    document.body.appendChild(clonedElement);
    clonedElement.style.margin = 0;
    clonedElement.style.width = 'max-content';

    domtoimage.toPng(clonedElement, {})
        .then(function (dataUrl) {
            const diagramName = document.getElementById('diagram_name');
            window.saveAs(dataUrl, diagramName.value + '.png'); //Отправка файла
        })
        .catch(function (error) {
            console.error('Произошла ошибка:', error);
        })
        .finally(function () {
            document.body.removeChild(clonedElement); //Удаление копии
        });
}

function exportPdf() {
    const preElement = document.querySelector('.mermaid')
    //Создание временной копии и удаление лишних границ
    const clonedElement = preElement.cloneNode(true);
    document.body.appendChild(clonedElement);
    clonedElement.style.margin = 0;
    clonedElement.style.width = 'max-content';

    domtoimage.toPng(clonedElement, {})
        .then(function (dataUrl) {
            const diagramName = document.getElementById('diagram_name');
            //Создание документа PDF
            const pdf = new jspdf.jsPDF();
            const pageSize = pdf.internal.pageSize; // Получение размера страницы
            //Размеры и положение изображения на листе
            pageSize.setWidth(clonedElement.offsetWidth);
            pageSize.setHeight(clonedElement.offsetHeight);
            pdf.addImage(dataUrl, 'PNG', 0, 0, clonedElement.offsetWidth, clonedElement.offsetHeight);
            pdf.save(diagramName.value + '.pdf'); //Отправка файла
        })
        .catch(function (error) {
            console.error('Произошла ошибка:', error);
        })
        .finally(function () {
            document.body.removeChild(clonedElement); //Удаление копии
        });
}

function exportSql(type) {
    location.href += '/export?type=' + type;
}