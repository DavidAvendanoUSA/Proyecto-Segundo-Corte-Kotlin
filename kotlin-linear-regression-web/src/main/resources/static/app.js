const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => Array.from(document.querySelectorAll(sel));

let chart;

function parsePoints(text) {
    const lines = text.split(/\r?\n/).map(l => l.trim()).filter(Boolean);
    const points = [];
    for (let i = 0; i < lines.length; i++) {
        const line = lines[i].replace(/\s*;\s*/g, ',').replace(/\s+/, ',');
        const parts = line.split(',').map(s => s.trim()).filter(Boolean);
        if (parts.length !== 2) {
            throw new Error(`Línea ${i + 1}: formato inválido. Usa "x,y"`);
        }
        const x = Number(parts[0]);
        const y = Number(parts[1]);
        if (!Number.isFinite(x) || !Number.isFinite(y)) {
            throw new Error(`Línea ${i + 1}: x e y deben ser numéricos`);
        }
        points.push({ x, y });
    }
    if (points.length < 2) throw new Error("Se requieren al menos 2 puntos");
    const distinctX = new Set(points.map(p => p.x));
    if (distinctX.size < 2) throw new Error("Todos los valores de X son idénticos");
    return points;
}

function showMessage(msg, ok = false) {
    const el = $("#messages");
    el.textContent = msg || "";
    el.style.color = ok ? "var(--ok)" : "var(--muted)";
}

function renderResult(res, points) {
    $("#equation").textContent = res.equation;
    $("#r2").textContent = res.r2 === null ? "—" : res.r2.toFixed(6);
    $("#n").textContent = res.n;

    const scatterData = points.map(p => ({ x: p.x, y: p.y }));
    const lineData = res.linePoints.map(p => ({ x: p.x, y: p.y }));

    const ctx = $("#chart").getContext("2d");
    if (chart) chart.destroy();
    chart = new Chart(ctx, {
        type: "scatter",
        data: {
            datasets: [
                {
                    label: "Puntos",
                    data: scatterData,
                    borderColor: "rgba(56,189,248,0.9)",
                    backgroundColor: "rgba(56,189,248,0.7)",
                    pointRadius: 4,
                    showLine: false,
                },
                {
                    label: "Recta de regresión",
                    data: lineData,
                    borderColor: "rgba(34,197,94,0.9)",
                    backgroundColor: "rgba(34,197,94,0.6)",
                    pointRadius: 0,
                    showLine: true,
                    parsing: false,
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                x: { type: "linear", title: { display: true, text: "X" } },
                y: { type: "linear", title: { display: true, text: "Y" } }
            },
            plugins: {
                legend: { labels: { color: "#e2e8f0" } }
            }
        }
    });
}

async function computeRegression(points) {
    const res = await fetch("/api/regression", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ points })
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || `Error HTTP ${res.status}`);
    }
    return res.json();
}

async function saveDataset(name, points) {
    const res = await fetch("/api/datasets", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, points })
    });
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || `Error HTTP ${res.status}`);
    }
    return res.json();
}

async function listDatasets() {
    const res = await fetch("/api/datasets");
    if (!res.ok) throw new Error(`Error HTTP ${res.status}`);
    return res.json();
}

async function getDataset(id) {
    const res = await fetch(`/api/datasets/${id}`);
    if (!res.ok) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || `Error HTTP ${res.status}`);
    }
    return res.json();
}

async function deleteDataset(id) {
    const res = await fetch(`/api/datasets/${id}`, { method: "DELETE" });
    if (!res.ok && res.status !== 204) {
        const err = await res.json().catch(() => ({}));
        throw new Error(err.error || `Error HTTP ${res.status}`);
    }
}

function attachHandlers() {
    $("#btnCalc").addEventListener("click", async () => {
        try {
            showMessage("");
            const points = parsePoints($("#pointsInput").value);
            const result = await computeRegression(points);
            renderResult(result, points);
            showMessage("Cálculo realizado correctamente.", true);
        } catch (e) {
            showMessage(e.message || String(e));
        }
    });

    $("#btnSave").addEventListener("click", async () => {
        try {
            showMessage("");
            const name = $("#datasetName").value.trim();
            if (!name) throw new Error("Ingresa un nombre para el dataset.");
            const points = parsePoints($("#pointsInput").value);
            const created = await saveDataset(name, points);
            renderResult(created.regression, created.points);
            $("#datasetName").value = "";
            await refreshList();
            showMessage("Dataset guardado correctamente.", true);
        } catch (e) {
            showMessage(e.message || String(e));
        }
    });

    $("#btnRefresh").addEventListener("click", refreshList);
}

async function refreshList() {
    try {
        const ul = $("#datasetList");
        ul.innerHTML = "<li>Cargando...</li>";
        const items = await listDatasets();
        if (!items.length) {
            ul.innerHTML = "<li>No hay datasets guardados.</li>";
            return;
        }
        ul.innerHTML = "";
        for (const it of items) {
            const li = document.createElement("li");
            const left = document.createElement("div");
            const right = document.createElement("div");
            left.innerHTML = `<strong>${it.name}</strong> <span class="meta">#${it.id}</span> <span class="badge">${it.count} pts</span>`;
            right.className = "actions";
            const btnLoad = document.createElement("button");
            btnLoad.textContent = "Cargar";
            btnLoad.addEventListener("click", async () => {
                try {
                    const ds = await getDataset(it.id);
                    $("#pointsInput").value = ds.points.map(p => `${p.x},${p.y}`).join("\n");
                    renderResult(ds.regression, ds.points);
                    showMessage(`Dataset "${ds.name}" cargado.`, true);
                } catch (e) {
                    showMessage(e.message || String(e));
                }
            });
            const btnDel = document.createElement("button");
            btnDel.textContent = "Eliminar";
            btnDel.addEventListener("click", async () => {
                if (!confirm(`¿Eliminar dataset "${it.name}" (#${it.id})?`)) return;
                try {
                    await deleteDataset(it.id);
                    await refreshList();
                    showMessage("Dataset eliminado.", true);
                } catch (e) {
                    showMessage(e.message || String(e));
                }
            });
            right.appendChild(btnLoad);
            right.appendChild(btnDel);
            li.appendChild(left);
            li.appendChild(right);
            ul.appendChild(li);
        }
    } catch (e) {
        showMessage(e.message || String(e));
    }
}

// Init
attachHandlers();
refreshList().catch(() => {});