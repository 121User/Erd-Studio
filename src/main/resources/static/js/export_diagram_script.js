//Экспорт
export function exportDiagram() {
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
    let diagramName = document.getElementById('diagram_name').value;
    const editor = window.myGlobalObject.editorObj;
    const diagramCode = getFormattedCodeForDB(editor.doc.getValue());
    if(diagramName === ''){
        diagramName = 'Новая диаграмма';
    }

    let text = '';
    if (type === 'mssql') {
        text = convertToMsSqlServer(diagramCode);
    } else if (type === 'postgresql') {
        text = convertToPostgreSql(diagramCode);
    }

    const sqlBlob = new Blob([text], { type: 'text/plain;charset=utf-8' });
    const sqlUrl = URL.createObjectURL(sqlBlob);//Создание URL-адреса
    window.saveAs(sqlUrl, diagramName + '.sql'); //Отправка файла
}

//Преобразование кода диаграммы для Ms Sql Server
function convertToMsSqlServer(code) {
    let refs = getMsSqlServerRefs(code);
    let result1 = code.replace(/pk/g, "PRIMARY KEY").replace(/PK/g, "PRIMARY KEY").replace(/\[/g, "")
        .replace(/not null/g, "NOT NULL").replace(/[\n\s]*}[\n\s]*/g, "\n)\nGO\n").replace(/ref:(.*?)[,\]]/g, "")
        .replace(/, /g, " ").replace(/]/g, "").replace(/( *)\{/g, "] (");

    let result = '';
    for (let line of result1.split('\n')) {
        if (line.endsWith('(')) {
            result += 'CREATE TABLE [' + line + '\n';
        } else if (!line.endsWith('])') && !line.includes('GO') && line !== '' && line !== ')') {
            let attributes = line.trim().split(/\s/);
            let attr = attributes[0];
            let str = line.replace(attr, "["+attr+"]").replace(/\s+$/, "");
            result += str + ",\n";
        } else {
            result += line + '\n';
        }
    }
    result = result.replaceAll(',\n)', '\n)')
    return result + refs;
}
//Получение строк создания связей для Ms Sql Server
function getMsSqlServerRefs(code){
    let refs = '';
    const lines = code.split('\n');

    for (let line of lines) {
        line = line.trim();
        const matcher = line.match(/ref: (.*?)[,\]]/);
        if (matcher) {
            const matchers = matcher[1].split(/\s/);
            refs += `\nALTER TABLE [${matchers[0]}] ADD FOREIGN KEY ([${line.split(/\s+/)[0]}]) ` +
                `REFERENCES [${matchers[2]}] ([${getPkRef(code, matchers[2])}])\nGO\n`;
        }
    }
    refs = refs.replace(/,/g, ''); // заменить все запятые на пустую строку
    return refs;
}

//Преобразование кода диаграммы для PostgreSQL
function convertToPostgreSql(code) {
    let refs = getPostgreSqlRefs(code);
    let result1 = code.replace(/pk/g, "PRIMARY KEY").replace(/PK/g, "PRIMARY KEY").replace(/\[/g, "")
        .replace(/not null/g, "NOT NULL").replace(/[\n\s]*}[\n\s]*/g, "\n);\n").replace(/ref:(.*?)[,\]]/g, "")
        .replace(/, /g, " ").replace(/]/g, "").replace(/( *)\{/g, '" (');

    let result = '';
    for (let line of result1.split('\n')) {
        if (line.endsWith('(')) {
            result += 'CREATE TABLE "' + line + '\n';
        } else if (!line.endsWith(');') && line !== '') {
            let attributes = line.trim().split(/\s/);
            let attr = attributes[0];
            let str = line.replace(attr, '"' + attr + '"').replace(/\s+$/, '');
            result += str + ',\n';
        } else {
            result += line + '\n';
        }
    }
    result = result.replaceAll(',\n);', '\n);')
    return result + refs;
}
//Получение строк создания связей для PostgreSQL
function getPostgreSqlRefs(code) {
    let refs = '';
    const strings = code.split('\n');

    for (let line of strings) {
        line = line.trim();
        const matcher = line.match(/ref: (.*?)[,\]]/);
        if (matcher) {
            const matchers = matcher[1].split(/\s/);
            refs += `\nALTER TABLE "${matchers[0]}" ADD FOREIGN KEY ("${line.split(/\s+/)[0]}") ` +
                `REFERENCES "${matchers[2]}" ("${getPkRef(code, matchers[2])}");\n`;
        }
    }
    refs = refs.replaceAll(',', '');
    return refs;
}

//Получение названия первичного ключа второй сущности для связи
function getPkRef(code, entity) {
    const lines = code.split('\n');
    for (let i = 0; i < lines.length; i++) {
        if (lines[i].startsWith(entity)) {
            return lines[i + 1].trim().split(/\s/)[0]; //Название атрибута со следующей строки после названия сущности
        }
    }
    return null;
}


//Обработка кода диаграммы для сохранения в базе данных
export function getFormattedCodeForDB(code) {
    let lines = code.split(/\n/g);
    for (let i = 0; i < lines.length; i++) {
        lines[i] = lines[i].trim();
        if (!lines[i].includes('{') && !lines[i].includes('}')) {
            lines[i] = '  ' + lines[i];
        }
    }
    return lines.join('\n');
}