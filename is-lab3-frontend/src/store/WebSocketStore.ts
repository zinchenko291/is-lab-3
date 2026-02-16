import { makeAutoObservable, runInAction } from 'mobx';
import type { RootStore } from './RootStore';
import type { VehicleDto } from '../api/models/vehicles';
import type { CoordinatesDto } from '../api/models/coordinates';
import type { ImportConflict, ImportOperation } from '../api/models/imports';

type WsAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'STATUS' | 'CONFLICT';

type VehicleEventMessage = {
  type: 'event';
  entity: 'VEHICLE';
  action: WsAction;
  id: number;
  payload: VehicleDto;
};

type CoordinateEventMessage = {
  type: 'event';
  entity: 'COORDINATES';
  action: WsAction;
  id: number;
  payload: CoordinatesDto;
};

type ImportEventMessage = {
  type: 'event';
  entity: 'IMPORT_OPERATION';
  action: WsAction;
  id: number;
  payload: ImportOperation;
};

type ImportConflictEventMessage = {
  type: 'event';
  entity: 'IMPORT_CONFLICT';
  action: WsAction;
  id: number;
  importId?: number;
  payload: ImportConflict;
};

type EventMessage =
  | VehicleEventMessage
  | CoordinateEventMessage
  | ImportEventMessage
  | ImportConflictEventMessage;

type PingMessage = {
  type: 'ping';
};

type WsMessage = EventMessage | PingMessage;

export class WebSocketStore {
  root: RootStore;

  socket: WebSocket | null = null;
  isConnected = false;
  lastError: string | null = null;
  lastImportConflict: { importId: number; conflictId: number } | null = null;

  constructor(root: RootStore) {
    this.root = root;

    makeAutoObservable(this, {
      root: false,
      socket: false,
    });
  }

  connect(url: string) {
    if (this.socket) {
      this.socket.close();
    }

    const ws = new WebSocket(url);
    this.socket = ws;

    ws.onopen = () => {
      runInAction(() => {
        this.isConnected = true;
        this.lastError = null;
      });

      this.send({
        type: 'ping',
      });
    };

    ws.onclose = () => {
      runInAction(() => {
        this.isConnected = false;
        this.socket = null;
      });
    };

    ws.onerror = (event) => {
      console.error('WebSocket error', event);
      runInAction(() => {
        this.lastError = 'Ошибка WebSocket соединения';
      });
    };

    ws.onmessage = (event) => {
      this.handleRawMessage(event.data);
    };
  }

  disconnect() {
    if (this.socket) {
      this.socket.close();
      this.socket = null;
    }
    this.isConnected = false;
  }

  send(data: unknown) {
    if (!this.socket || this.socket.readyState !== WebSocket.OPEN) return;
    this.socket.send(JSON.stringify(data));
  }

  private handleRawMessage(raw: any) {
    let msg: WsMessage | null = null;

    try {
      msg = JSON.parse(raw);
    } catch (e) {
      console.warn('Invalid WS message', raw);
      return;
    }

    if (!msg || !msg.type) return;

    if (msg.type === 'ping') {
      return;
    }

    if (msg.type === 'event') {
      this.handleEvent(msg);
    }
  }

  private handleEvent(msg: EventMessage) {
    const handlers: Record<EventMessage['entity'], (m: any) => void> = {
      VEHICLE: (m: VehicleEventMessage) => this.applyVehicleEvent(m),
      COORDINATES: (m: CoordinateEventMessage) => this.applyCoordinateEvent(m),
      IMPORT_OPERATION: (m: ImportEventMessage) => this.applyImportEvent(m),
      IMPORT_CONFLICT: (m: ImportConflictEventMessage) =>
        this.applyImportConflictEvent(m),
    };

    const handler = handlers[msg.entity];
    handler?.(msg as any);
  }

  private applyCollectionEvent<T extends { id: number }>(
    items: T[],
    getTotal: () => number,
    setTotal: (value: number) => void,
    action: WsAction,
    id: number,
    payload: T
  ) {
    const index = items.findIndex((item) => item.id === id);

    const actionHandlers: Record<WsAction, () => void> = {
      CREATE: () => {
        if (index >= 0) {
          items[index] = payload;
        } else {
          items.unshift(payload);
          setTotal(getTotal() + 1);
        }
      },
      UPDATE: () => {
        if (index < 0) return;
        items[index] = payload;
      },
      DELETE: () => {
        if (index < 0) return;
        items.splice(index, 1);
        const total = getTotal();
        if (total > 0) setTotal(total - 1);
      },
    };

    actionHandlers[action]?.();
  }

  private applyVehicleEvent(msg: VehicleEventMessage) {
    const store = this.root.vehiclesStore;
    const { action, payload, id } = msg;

    runInAction(() => {
      this.applyCollectionEvent(
        store.vehicles,
        () => store.total,
        (v) => {
          store.total = v;
        },
        action,
        id,
        payload
      );
    });
  }

  private applyCoordinateEvent(msg: CoordinateEventMessage) {
    const store = this.root.coordinatesStore;
    const { action, payload, id } = msg;

    runInAction(() => {
      this.applyCollectionEvent(
        store.coordinates,
        () => store.total,
        (v) => {
          store.total = v;
        },
        action,
        id,
        payload
      );
    });
  }

  private applyImportEvent(msg: ImportEventMessage) {
    const store = this.root.importsStore;
    const { action, payload, id } = msg;

    runInAction(() => {
      store.applyImportEvent(action, id, payload);

      if (action === 'CONFLICT') {
        this.lastImportConflict = { importId: id, conflictId: id };
      }
    });
  }

  private resolveConflictImportId(msg: ImportConflictEventMessage) {
    const payload: any = msg.payload;
    return (
      msg.importId ??
      payload?.importId ??
      payload?.operationId ??
      payload?.importOperationId ??
      null
    );
  }

  private applyImportConflictEvent(msg: ImportConflictEventMessage) {
    const store = this.root.importsStore;
    const { action, payload, id } = msg;
    const importId = this.resolveConflictImportId(msg);

    runInAction(() => {
      store.applyConflictEvent(action, payload, importId);

      const shouldNotify =
        action === 'CREATE' ||
        action === 'CONFLICT' ||
        payload?.resolution === 'UNRESOLVED';

      if (shouldNotify && importId !== null) {
        this.lastImportConflict = { importId, conflictId: id };
      }
    });
  }

  clearLastImportConflict() {
    this.lastImportConflict = null;
  }
}
