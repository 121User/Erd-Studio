import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';

// Создание диаграммы на странице diagram_working,
// настройка масштабирования и передвижения,
// установка прослушивателей для обновления диаграммы

//Отображение диаграммы
export async function drawDiagram() {
    //Отображение диаграммы или вывод сообщения об ошибке
    try {
        document.getElementById('diagram').style.display = 'block';
        document.getElementById('error_text').style.display = 'none';
        const diagram = document.getElementById('diagram');
        const editor = window.myGlobalObject.editorObj;
        const diagramCode = editor.doc.getValue();
        const graphDefinition = getFormattedCodeForDiagram(diagramCode);

        //Выбор темы
        let designTheme = 'default'
        if (document.getElementById('checkbox_theme').checked) {
            designTheme = 'dark';
        }
        mermaid.initialize({startOnLoad: true, theme: designTheme});

        //Генерация изображения по коду диаграммы
        const {svg} = await mermaid.render('graphDiv', graphDefinition); //Деструктуризация (возвращает свойство svg возвращенного объекта)
        diagram.innerHTML = svg;
        configPanZoomDiagram();
    } catch (err) {
        console.warn(err)
        document.getElementById('diagram').style.display = 'none';
        document.getElementById('error_text').style.display = 'block';
    }
}

//Настройка масштабирования и панорамирования (передвижения) диаграммы
function configPanZoomDiagram() {
    try {
        //Указание размеров диаграммы
        const diagram = document.getElementById('diagram');
        const svg = diagram.querySelector('svg');
        svg.style.maxWidth = 'none';
        svg.style.height = '100%';

        //Масштабирования и панорамирования (передвижения) диаграммы
        const panZoom = window.svgPanZoom(svg, {
            controlIconsEnabled: true, //Показывать элементы управления
            minZoom: 0.09, //Минимальный масштаб
            maxZoom: 10, //Максимальный масштаб
            zoomScaleSensitivity: 0.4 //Чувствительность колеса мыши
        })
    } catch (err) {
        console.warn('Ошибка в коде диаграммы')
    }
}


//Обработка кода для построения диаграммы
function getFormattedCodeForDiagram(code) {
    code = code.replace(/\/\/.*/g, ''); //Скрытие комментариев при отображении диаграммы
    let refs = getRefs(code)
    let result = 'erDiagram\n' + code
        .replace(/pk/g, 'PK').replace(/\[/g, '').replace(/not null/g, '"NN"').replace(/ ref:/g, ', ref:')
        .replace(/ref:(.*?)[,\]]/g, 'FK').replace(/, /g, ' ').replace(/]/g, '');

    return result + refs;
}

//Получение связи для построения диаграммы
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
