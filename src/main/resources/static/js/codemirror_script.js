import 'https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.63.1/codemirror.min.js';
import 'https://cdnjs.cloudflare.com/ajax/libs/codemirror/5.63.1/mode/javascript/javascript.min.js';

//Скрипт для редактора кода

//Создание редактора кода
export function createCodeMirror(isReadOnly) {
    //Загрузка нового режима CodeMirror
    CodeMirror.defineMode("custom-mode", createMode);
    //Создание редактора кода
    let codeTextarea = document.getElementById('diagram_code');
    let editor = CodeMirror.fromTextArea(codeTextarea, {
        lineNumbers: true, //Панель номеров строк
        mode: 'custom-mode', //Стиль выделения,
        readOnly: isReadOnly, //Доступ только на чтение
        theme: 'custom-theme', //Тема оформления
        extraKeys: {
            //Обработка отступов по символам '{' и '}'
            "Shift-[": function (cm) {
                cm.replaceSelection("{\n  ", 'end');
            },
            "Shift-]": function (cm) {
                cm.replaceSelection("}\n", 'end');
            },
            //Дублирование выделонного фрагмента кода
            "Ctrl-D": function (cm) {
                const selection = cm.getSelection();
                const selStart = cm.getCursor("start");
                const selEnd = cm.getCursor("end");
                cm.replaceSelection(selection + selection); //Дублирование
                cm.setSelection(selStart, selEnd); //Восстановление выделения
            },
            //Комментирование строк
            "Ctrl-/": function (cm) {
                const selStart = editor.getCursor("start");
                const selEnd = editor.getCursor("end");

                const fullSelStart = {line: selStart.line, ch: 0}; //Выделение первой строки полностью
                cm.setSelection(fullSelStart, selEnd);
                const selection = cm.getSelection();

                const modifiedLines = selection.split('\n').map(line => {
                    if (line.trim().startsWith("//")) {
                        return line.replace('//', ''); //Раскомментирование
                    } else {
                        return "//" + line;  //Комментирование
                    }
                });

                const modifiedSelection = modifiedLines.join("\n");
                cm.replaceSelection(modifiedSelection);
                cm.setSelection(selStart, selEnd); //Восстановление выделения
            },

        },
    });
    editor.on("keydown", function (cm, event) {
        if (event.key === "Backspace") {
            const code = cm.getValue();
            if (code.endsWith('{\n  ')) {
                cm.setValue(code.replace(/[\s\n]*$/gm, ''));
                cm.execCommand("goDocEnd"); //Перемещение каретки в конец текста
            }
        }
    });

    return editor;
}

//Создание режима для выделения слов в CodeMirror
function createMode() {
    const keywords = {
        "ref:": true, "not null": true, "PK": true, "pk": true, "<": true, "-": true,
    };
    const keychars = {
        "{": true, "}": true,
        "[": true, "]": true,
        "(": true, ")": true,
        ",": true,
    };
    return {
        token: function (stream) {
            //Выделение ключевых слов
            for (let word in keywords) {
                if (stream.match(word)) {
                    return "custom-keyword"; //Название стиля (.cm-custom-keyword)
                }
            }
            //Выделение ключевых символов
            for (let char in keychars) {
                if (stream.match(char)) {
                    return "custom-comment"; //Название стиля (.cm-custom-comment)
                }
            }
            //Выделение чисел
            if (stream.match(/\d+/)) {
                return "custom-digit"; //Название стиля (.cm-custom-digit)
            }
            //Выделение комментариев
            if (stream.match(/\/\/.*/)) {
                stream.skipToEnd();
                return "custom-comment"; //Название стиля (.cm-custom-comment)
            }
            stream.next();
            return null;
        }
    };
}