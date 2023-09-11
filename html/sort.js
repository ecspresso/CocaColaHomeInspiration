function checkJSON(json) {
    if(json == null) {
        json = [];
    } else {
        try {
            json = JSON.parse(json);
        } catch (error){
            console.error(error);
            json = [];
        }
    }

    return json;
}

function getFilterKeys() {
    let filterKeys = checkJSON(localStorage.getItem("filterKeys"));
    if(filterKeys.length === 0) {
        setFilterKeys();
        return getFilterKeys();
    } else {
        return filterKeys;
    }
}

function getFilter() {
    return checkJSON(localStorage.getItem("filter"));
}

function toggleFilter(source, buildingName) {
    source.classList.toggle("active");
    let filter = getFilter();

    if(!filter.includes(buildingName)) {
        filter.push(buildingName);
    } else {
        let index = filter.indexOf(buildingName);
        filter.splice(index, 1);
    }

    localStorage.setItem("filter", JSON.stringify(filter));

    applyFilter();
}

function applyFilter() {
    let filter = getFilter();

    if(filter.length === 0) {
        removeAllDisplayRules();
    } else {
        let filterKeys = getFilterKeys();

        for(let key in filterKeys) {
            hide(filterKeys[key]);
        }

        for(let key in filter) {
            show(filter[key]);
        }
    }
}


function hide(buildingName) {
    let sheet = window.document.styleSheets[0];
    sheet.insertRule('[data-byggnad="'+buildingName+'"] { display: none; }', sheet.cssRules.length);
}

function show(buildingName) {
    let sheet = window.document.styleSheets[0];

    for(let i = sheet.cssRules.length - 1; i >= 0 ;i--) {
        if(sheet.cssRules[i].selectorText === '[data-byggnad="'+buildingName+'"]') {
            sheet.deleteRule(i);
        }
    }
}

function removeAllDisplayRules() {
    let rules = [];
    let sheet = document.styleSheets[0];

    getFilterKeys().forEach((key) => rules.push('[data-byggnad="' + key + '"]'))


    for(let i = sheet.cssRules.length - 1; i >= 0; i--) {
        let selectorText = sheet.cssRules.item(i).selectorText;

        if(selectorText != null && rules.indexOf(selectorText) !== -1) {
            sheet.deleteRule(i);
        }
    }

    localStorage.removeItem("filter");
    document.querySelectorAll("#buttons > button").forEach((ele) => ele.classList.remove("active"));
}

applyFilter();
let f = getFilter();

document.querySelectorAll("#buttons > button").forEach((ele) => {
  if(f.indexOf(ele.textContent) !== -1) {
   	ele.classList.add("active");
  }
})