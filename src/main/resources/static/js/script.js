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
function searchDiagramByKeyup(e) {
    if (e.keyCode === 13) {
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

//Активация поиска при нажатии на Enter
function saveCodeByKeyup(e) {
    if (e.keyCode === 13) {
        saveCode();
    }
}

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
    const svgURL = URL.createObjectURL(svgBlob);//Создание URL-адреса для объекта Blob

    const link = document.createElement('a');
    link.href = svgURL;
    link.download = diagramName.value + '.svg'; //Имя файла

    link.click();
    URL.revokeObjectURL(svgURL); //Очистка созданного URL
}

function exportPng() {
    const diagramName = document.getElementById('diagram_name');
    const diagram = document.getElementById('diagram');
    const canvas = document.createElement('canvas'); //Создание временного элемента canvas
    canvas.width = diagram.offsetWidth;
    canvas.height = diagram.offsetHeight;

    // Преобразование SVG в изображение PNG
    html2canvas(diagram, {canvas})
        .then(function (canvas) {
            const pngURL = canvas.toDataURL('image/png');//Получение URL-адреса изображения PNG

            const link = document.createElement('a');
            link.href = pngURL;
            link.download = diagramName.value + '.png'; //Имя файла

            link.click();
        });
}

function exportPdf() {
    const diagramName = document.getElementById('diagram_name');
    const diagram = document.getElementById('diagram');
    const canvas = document.createElement('canvas'); //Создание временного элемента canvas
    canvas.width = diagram.offsetWidth;
    canvas.height = diagram.offsetHeight;

    // Преобразование SVG в изображение PNG
    html2canvas(diagram, {canvas})
        .then(function (canvas) {
            //Создание документа PDF
            const pdf = new jspdf.jsPDF();
            pdf.addImage(canvas, 'PNG', -100, 0);
            const pdfBlob = pdf.output('blob');
            const pdfURL = URL.createObjectURL(pdfBlob);

            const link = document.createElement('a');
            link.href = pdfURL;
            link.download = diagramName.value + '.pdf'; //Имя файла
            link.click();
            URL.revokeObjectURL(pdfURL);
        });
}

function exportSql(type) {
    location.href += '/export?type=' + type;
}